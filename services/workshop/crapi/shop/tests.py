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
from utils.mock_methods import get_sample_user_data, mock_jwt_auth_required

patch("utils.jwt.jwt_auth_required", mock_jwt_auth_required).start()

import logging
import bcrypt
import json
from django.test import TestCase, Client
from django.utils import timezone
from utils import messages
from crapi.user.models import User, UserDetails
from crapi.shop.models import Coupon

logger = logging.getLogger("ProductTest")


class ProductTestCase(TestCase):
    """
    contains all the test cases related to Products
    Attributes:
        client: Client object used for testing
        user: dummy user object
        auth_headers: Auth headers for dummy user
        product_id: Product Identifier of the product created
        order_id: Order Identifier of the prder created
    """

    databases = "__all__"

    def setUp(self):
        """
        stores a sample request body for user signup
        creates a dummy user corresponding auth tokens
        :return: None
        """
        self.client = Client()

        self.coupon = Coupon.objects.using("mongodb").create(
            coupon_code="TRAC075", amount=75
        )

        self.coupon = Coupon.objects.using("mongodb").create(
            coupon_code="TRAC100", amount=100
        )

        user_data = get_sample_user_data()
        self.user = User.objects.create(
            email=user_data["email"],
            number=user_data["number"],
            password=bcrypt.hashpw(
                user_data["password"].encode("utf-8"), bcrypt.gensalt()
            ).decode(),
            role=User.ROLE_CHOICES.USER,
            created_on=timezone.now(),
        )

        self.user_details = UserDetails.objects.create(
            available_credit=100,
            name=user_data["name"],
            status="ACTIVE",
            user=self.user,
        )

        self.auth_headers = {"HTTP_AUTHORIZATION": "Bearer " + user_data["email"]}

    def add_product(self):
        """
        creates a dummy product with add_product api with an image
        should get a valid response saying product created
        :return: None
        """
        product_details = {
            "name": "test_Seat",
            "price": 10,
            "image_url": "https://4.imimg.com/data4/NI/WE/MY-19393581/ciaz-car-seat-cover-500x500.jpg",
        }
        res = self.client.post(
            "/workshop/api/shop/products", product_details, **self.auth_headers
        )
        self.product_id = json.loads(res.content)["id"]
        self.assertEqual(res.status_code, 200)

    def apply_coupon(self):
        """
        applies a coupon to the dummy user using apply_coupon api
        should get a valid response saying coupon applied
        :return: None
        """
        coupon_details = {"coupon_code": "TRAC075", "amount": 75}
        res = self.client.post(
            "/workshop/api/shop/apply_coupon",
            data=coupon_details,
            content_type="application/json",
            **self.auth_headers
        )
        logger.info(res.json())
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.json()["message"], messages.COUPON_APPLIED)

    def test_apply_coupon_twice(self):
        """
        applies a coupon twice to the dummy user using apply_coupon api
        should get a error response saying coupon already applied
        :return: None
        """
        coupon_details = {"coupon_code": "TRAC100", "amount": 100}
        res = self.client.post(
            "/workshop/api/shop/apply_coupon",
            data=coupon_details,
            content_type="application/json",
            **self.auth_headers
        )
        res = self.client.post(
            "/workshop/api/shop/apply_coupon",
            data=coupon_details,
            content_type="application/json",
            **self.auth_headers
        )
        logger.info(res.json())
        self.assertEqual(res.status_code, 400)
        self.assertEqual(
            res.json()["message"],
            coupon_details["coupon_code"] + " " + messages.COUPON_ALREADY_APPLIED,
        )

    def test_invalid_coupon(self):
        """
        applies an invalid coupon to the dummy user using apply_coupon api
        should get a valid response saying coupon is invalid
        :return: None
        """
        coupon_details = {"coupon_code": "TRAC105", "amount": 75}
        res = self.client.post(
            "/workshop/api/shop/apply_coupon",
            data=coupon_details,
            content_type="application/json",
            **self.auth_headers
        )
        logger.info(res.json())
        self.assertEqual(res.status_code, 400)
        self.assertEqual(res.json()["message"], messages.COUPON_NOT_FOUND)

    def test_sql_injection(self):
        """
        applies a SQLI query to the  apply_coupon api
        should get a valid response and a message that proves SQLI successfully exploited
        :return: None
        """
        coupon_details = {
            "coupon_code": "'; SELECT number FROM user_login WHERE email='"
            + self.user.email,
            "amount": 75,
        }
        res = self.client.post(
            "/workshop/api/shop/apply_coupon",
            data=coupon_details,
            content_type="application/json",
            **self.auth_headers
        )
        logger.info(res.json())
        self.assertEqual(res.status_code, 400)
        self.assertEqual(
            res.json()["message"],
            self.user.number + " " + messages.COUPON_ALREADY_APPLIED,
        )

    def create_order(self):
        """
        creates an order using the product_id from the previous test.
        should get a valid response saying product created
        :return: order details
        """
        order_details = {"product_id": str(self.product_id), "quantity": 1}
        res = self.client.post(
            "/workshop/api/shop/orders",
            order_details,
            content_type="application/json",
            **self.auth_headers
        )
        self.order_id = json.loads(res.content)["id"]
        self.assertEqual(res.status_code, 200)

    def test_unauthenticated_get_order(self):
        """
        retrieves the order based on order id.
        should get a valid response saying order retrieved.
        :return: order details
        """
        self.add_product()
        self.apply_coupon()
        self.create_order()
        res = self.client.get("/workshop/api/shop/orders/" + str(self.order_id))
        self.assertEqual(res.status_code, 200)
