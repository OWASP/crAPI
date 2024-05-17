#
# Licensed under the Apache License, Version 2.0 (the “License”);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an “AS IS” BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""
contains all the test cases related to shop management
"""
from unittest.mock import patch

from django.db import connection
from utils.mock_methods import (
    get_sample_admin_user,
    get_sample_user_data,
    get_sample_users,
    mock_jwt_auth_required,
)

patch("utils.jwt.jwt_auth_required", mock_jwt_auth_required).start()

import logging
import bcrypt
import json
from django.test import TestCase, Client
from django.utils import timezone
from utils import messages
from crapi_site import settings
from crapi.user.models import User, UserDetails

logger = logging.getLogger("UserTest")
MAX_USER_COUNT = 40


class UserDetailsTestCase(TestCase):
    """
    contains all the test cases related to UserDetails
    Attributes:
        client: Client object used for testing
        user: dummy user object
        auth_headers: Auth headers for dummy user
    """

    databases = "__all__"
    setup_done = False

    def setUp(self):
        self.client = Client()
        user_data = get_sample_admin_user()
        uset = User.objects.filter(email=user_data["email"])
        if not uset.exists():
            user = User.objects.create(
                email=user_data["email"],
                number=user_data["number"],
                password=bcrypt.hashpw(
                    user_data["password"].encode("utf-8"), bcrypt.gensalt()
                ).decode(),
                role=user_data["role"],
                created_on=timezone.now(),
            )
            user_detail = UserDetails.objects.create(
                available_credit=100, name=user_data["name"], status="ACTIVE", user=user
            )
            user.save()
            user_detail.save()
        self.auth_headers = {"HTTP_AUTHORIZATION": "Bearer " + user_data["email"]}

    def setup_database(self):
        self.users_data = get_sample_users(MAX_USER_COUNT)
        for user_data in self.users_data:
            uset = User.objects.filter(email=user_data["email"])
            if not uset.exists():
                try:
                    cursor = connection.cursor()
                    cursor.execute("select nextval('user_login_id_seq')")
                    result = cursor.fetchone()
                    user_id = result[0]
                except Exception as e:
                    logger.error("Failed to fetch user_login_id_seq" + str(e))
                    user_id = 1
                user_i = User.objects.create(
                    id=user_id,
                    email=user_data["email"],
                    number=user_data["number"],
                    password=bcrypt.hashpw(
                        user_data["password"].encode("utf-8"), bcrypt.gensalt()
                    ).decode(),
                    role=user_data["role"],
                    created_on=timezone.now(),
                )
                user_details_i = UserDetails.objects.create(
                    available_credit=100,
                    name=user_data["name"],
                    status="ACTIVE",
                    user=user_i,
                )
                user_i.save()
                user_details_i.save()
                logger.info("Created user with id: " + str(user_id))

    def test_get_api_management_users_all(self):
        """
        tests the get user details api
        :return: None
        """
        self.setup_database()
        response = self.client.get(
            "/workshop/api/management/users/all", **self.auth_headers
        )
        self.assertEqual(response.status_code, 200)
        response_data = json.loads(response.content)
        all_users_length = len(response_data["users"])
        self.assertEqual(
            len(response_data["users"]), min(all_users_length, settings.MAX_LIMIT)
        )
        response = self.client.get(
            "/workshop/api/management/users/all?limit=10&offset=0", **self.auth_headers
        )
        self.assertEqual(response.status_code, 200)
        response_data = json.loads(response.content)
        self.assertEqual(len(response_data["users"]), 10)
        response2 = self.client.get(
            "/workshop/api/management/users/all?limit=10&offset=10", **self.auth_headers
        )
        self.assertEqual(response2.status_code, 200)
        response_data2 = json.loads(response2.content)
        self.assertNotEquals(response_data["users"], response_data2["users"])
        response = self.client.get(
            "/workshop/api/management/users/all?limit=a&offset=-1", **self.auth_headers
        )
        self.assertEqual(response.status_code, 200)
        response_data = json.loads(response.content)
        self.assertEqual(len(response_data["users"]), all_users_length)

    def test_bad_get_api_management_users_all(self):
        """
        tests the get user details api
        :return: None
        """
        response = self.client.get("/workshop/api/management/users/all")
        self.assertEqual(response.status_code, 401)
