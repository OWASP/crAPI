# Copyright 2020 Traceable, Inc.
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
contains all the test cases related to mechanic
"""
from django.test import TestCase, Client
from datetime import datetime, timedelta
from utils.jwt import get_jwt
from utils.sample_data import get_sample_mechanic_data
from utils import messages
from crapi.mechanic.models import Mechanic
from user.models import User


class MechanicSignUpTestCase(TestCase):
    """
    contains all the test cases related to Mechanic SignUp
    Attributes:
        client: Client object used for testing
        mechanic: sample mechanic sign up request body
    """
    def setUp(self):
        """
        stores a sample request body for mechanic signup
        :return: None
        """
        self.client = Client()
        self.mechanic = get_sample_mechanic_data()

    def test_duplicate_email_signup(self):
        """
        creates a dummy mechanic through mechanic signup
        should get a valid response on first signup
        tries to create one more mechanic with the same email id
        should get an error response saying email already registered
        :return: None
        """
        res = self.client.post(
            '/api/mechanic/signup',
            self.mechanic,
            content_type="application/json"
        )
        self.assertEqual(res.status_code, 200)
        res = self.client.post(
            '/api/mechanic/signup',
            self.mechanic,
            content_type="application/json"
        )
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()['message'], messages.EMAIL_ALREADY_EXISTS)

    def test_duplicate_mechanic_code(self):
        """
        creates a dummy mechanic through mechanic signup
        should get a valid response on first signup
        tries to create one more mechanic with the same mechanic_code
        should get an error response saying mechanic_code already exists
        :return: None
        """
        res = self.client.post(
            '/api/mechanic/signup',
            self.mechanic,
            content_type="application/json"
        )
        self.assertEqual(res.status_code, 200)

        self.mechanic['email'] = 'abcd@example.com'
        res = self.client.post(
            '/api/mechanic/signup',
            self.mechanic,
            content_type="application/json"
        )
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()['message'], messages.MEC_CODE_ALREADY_EXISTS)

    def test_no_duplicate(self):
        """
        creates a dummy mechanic through mechanic signup
        should get a valid response on first signup
        tries to create one more mechanic with different email and mechanic_code
        should get a valid response(200) on second signup also
        :return:
        """
        res = self.client.post(
            '/api/mechanic/signup',
            self.mechanic,
            content_type="application/json"
        )
        self.assertEqual(res.status_code, 200)

        self.mechanic['email'] = 'abcd@example.com'
        self.mechanic['mechanic_code'] = 'TRAC_MEC_4'
        res = self.client.post(
            '/api/mechanic/signup',
            self.mechanic,
            content_type="application/json"
        )
        self.assertEqual(res.status_code, 200)
        self.assertIn(messages.MEC_CREATED.split(':')[0], res.json()['message'])

    def test_jwt_token(self):
        """
        creates a dummy mechanic through mechanic signup
        should get a valid response on signup
        tries to access secure api without token
        should get an error response saying jwt token required
        then tries to access secure api with a valid token
        should get a valid response(200) of the api
        :return: None
        """
        self.client.post('/api/mechanic/signup', self.mechanic, content_type="application/json")
        user = User.objects.get(email=self.mechanic['email'])

        res = self.client.get('/api/mechanic')
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()['message'], messages.JWT_REQUIRED)

        jwt_token = get_jwt(user)
        auth_headers = {'HTTP_AUTHORIZATION': 'Bearer ' + jwt_token}
        res = self.client.get('/api/mechanic', **auth_headers)
        self.assertEqual(res.status_code, 200)

    def test_expired_jwt_token(self):
        """
        creates a dummy mechanic through mechanic signup
        should get a valid response on signup
        tries to access secure api with an expired token
        should get an error response saying token expired
        :return: None
        """
        self.client.post('/api/mechanic/signup', self.mechanic, content_type="application/json")
        user = User.objects.get(email=self.mechanic['email'])

        res = self.client.get('/api/mechanic')
        self.assertNotEqual(res.status_code, 200)

        jwt_token = get_jwt(user, exp=datetime.utcnow()-timedelta(hours=3))
        auth_headers = {'HTTP_AUTHORIZATION': 'Bearer ' + jwt_token}
        res = self.client.get('/api/mechanic', **auth_headers)
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()['message'], messages.TOKEN_EXPIRED)

    def test_bad_request(self):
        """
        deletes password field from signup request body
        should get a bad request response
        :return: None
        """
        del[self.mechanic['password']]
        res = self.client.post(
            '/api/mechanic/signup',
            self.mechanic,
            content_type="application/json"
        )
        self.assertNotEqual(res.status_code, 200)
