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
import bcrypt
from django.test import TestCase, Client
from django.utils import timezone
from utils import messages
from utils.jwt import get_jwt
from utils.sample_data import get_sample_user_data
from user.models import User, UserDetails
from crapi.shop.models import Coupon

class ProductTestCase(TestCase):
    """
    contains all the test cases related to Products
    Attributes:
        client: Client object used for testing
        mechanic: sample mechanic sign up request body
        user: dummy mechanic object
        auth_headers: Auth headers for dummy mechanic
    """

    databases = '__all__'

    def setUp(self):
        """
        stores a sample request body for mechanic signup
        creates a dummy mechanic corresponding auth tokens
        :return: None
        """
        self.client = Client()

        self.coupon = Coupon.objects.using('mongodb').create(
            coupon_code = 'TRAC075',
            amount = 75
        )

        user_data = get_sample_user_data()
        self.user = User.objects.create(
            email = user_data['email'],
            number = user_data['number'],
            password=bcrypt.hashpw(
                user_data['password'].encode('utf-8'),
                bcrypt.gensalt()
            ).decode(),
            role=User.ROLE_CHOICES.USER,
            created_on=timezone.now()
        )

        self.user_details = UserDetails.objects.create(
            available_credit = 100,
            name = user_data['name'],
            status = 'ACTIVE',
            user = self.user
        )

        jwt_token = get_jwt(self.user)
        self.auth_headers = {'HTTP_AUTHORIZATION': 'Bearer ' + jwt_token}

    def test_add_products(self):
        """
        creates a dummy product with add_product api with an image
        should get a valid response saying product created
        :return: None
        """
        product_details = {
            'name': 'test_Seat',
            'price': 10,
            'image_url': 'https://4.imimg.com/data4/NI/WE/MY-19393581/ciaz-car-seat-cover-500x500.jpg',
        }
        res = self.client.post('/workshop/api/shop/products', product_details, **self.auth_headers)
        self.assertEqual(res.status_code, 200)

    def test_apply_coupon(self):
        """
        applies a coupon to the dummy user using apply_coupon api
        should get a valid response saying coupon applied
        :return: None
        """
        coupon_details = {
            'coupon_code': 'TRAC075',
            'amount': 75
        }
        res = self.client.post('/workshop/api/shop/apply_coupon', data=coupon_details, content_type='application/json', **self.auth_headers)
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.json()['message'], messages.COUPON_APPLIED)
