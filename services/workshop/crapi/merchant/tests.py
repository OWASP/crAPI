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
contains all the test cases related to merchant
"""
from unittest.mock import patch
from utils.mock_methods import get_sample_mechanic_data, mock_jwt_auth_required, get_sample_user_data

patch('utils.jwt.jwt_auth_required', mock_jwt_auth_required).start()

import bcrypt
from django.test import TestCase, Client
from django.utils import timezone
from utils import messages
from crapi.user.models import User, Vehicle, VehicleModel, VehicleCompany


class MerchantTestCase(TestCase):
    """
    contains all the test cases related to merchant
    Attributes:
        client: Client object used for testing
        mechanic: sample mechanic sign up request body
        user: dummy user object
        user_auth_headers: Auth headers for dummy user
        mechanic_auth_headers: Auth headers for dummy mechanic
        vehicle: dummy vehicle object
        contact_mechanic_request_body: sample contact mechanic request body
    """

    def setUp(self):
        """
        stores a sample request body for mechanic signup
        creates a dummy mechanic, a dummy user, a dummy vehicle and corresponding auth tokens
        stores a sample request body for contact mechanic
        :return: None
        """
        self.client = Client()
        self.mechanic = get_sample_mechanic_data()
        self.client.post('/workshop/api/mechanic/signup',
                         self.mechanic,
                         content_type="application/json")

        user_data = get_sample_user_data()
        self.user = User.objects.create(
            id=2,
            email=user_data['email'],
            number=user_data['number'],
            password=bcrypt.hashpw(user_data['password'].encode('utf-8'),
                                   bcrypt.gensalt()).decode(),
            role=User.ROLE_CHOICES.USER,
            created_on=timezone.now())
        self.user_auth_headers = {
            'HTTP_AUTHORIZATION': 'Bearer ' + user_data['email']
        }

        self.mechanic_auth_headers = {
            'HTTP_AUTHORIZATION': 'Bearer ' + self.mechanic['email']
        }

        self.vehicle_company = VehicleCompany.objects.create(
            name='RandomCompany')

        self.vehicle_model = VehicleModel.objects.create(
            fuel_type='1',
            model='NewModel',
            vehicle_img='Image',
            vehiclecompany=self.vehicle_company)

        self.vehicle = Vehicle.objects.create(pincode='1234',
                                              vin='9NFXO86WBWA082766',
                                              year='2020',
                                              status='ACTIVE',
                                              owner=self.user,
                                              vehicle_model=self.vehicle_model)
        self.contact_mechanic_request_body = {
            'mechanic_api': 'https://www.google.com',
            'repeat_request_if_failed': True,
            'number_of_repeats': 5,
            'mechanic_code': self.mechanic['mechanic_code'],
            'vin': self.vehicle.vin,
            'problem_details': 'My Car is not working',
        }

    def test_max_retries_exceeded(self):
        """
        increases number_of_repeats to 110
        should get an error message
        :return: None
        """
        self.contact_mechanic_request_body['number_of_repeats'] = 110
        res = self.client.post('/workshop/api/merchant/contact_mechanic',
                               self.contact_mechanic_request_body,
                               **self.user_auth_headers,
                               content_type="application/json")
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()['message'],
                         messages.NO_OF_REPEATS_EXCEEDED)

    def test_wrong_mechanic_api(self):
        """
        changes mechanic_api to an invalid url
        should get an error message
        :return: None
        """
        self.contact_mechanic_request_body['mechanic_api'] = \
            'https://jsonplaceholder.typicode.com/post'
        res = self.client.post('/workshop/api/merchant/contact_mechanic',
                               self.contact_mechanic_request_body,
                               **self.user_auth_headers,
                               content_type="application/json")
        self.assertNotEqual(res.status_code, 200)

    def test_contact_mechanic(self):
        """
        gives all correct field for contact mechanic
        should get a valid response from mechanic_api
        :return: None
        """
        res = self.client.post('/workshop/api/merchant/contact_mechanic',
                               self.contact_mechanic_request_body,
                               **self.user_auth_headers,
                               content_type="application/json")
        self.assertEqual(res.status_code, 200)
        self.assertIn('<title>Google</title>',
                      res.json()['response_from_mechanic_api'])

    def test_repeat_missing_request(self):
        """
        deletes repeat_request_if_failed field from contact_mechanic request body
        should get a bad request response
        :return: None
        """
        del self.contact_mechanic_request_body['repeat_request_if_failed']
        res = self.client.post('/workshop/api/merchant/contact_mechanic',
                               self.contact_mechanic_request_body,
                               **self.user_auth_headers,
                               content_type="application/json")
        self.assertEqual(res.status_code, 200)

    def test_receive_report_and_get_report(self):
        """
        tests receive_report with a valid request
        should get a valid response saying report sent
        should get a report_link

        tests the report_link
        should get the same report which is sent earlier

        tests if the mechanic also gets the same report
        should get the same report
        :return: None
        """
        res = self.client.get('/workshop/api/mechanic/receive_report',
                              self.contact_mechanic_request_body,
                              **self.user_auth_headers,
                              content_type="application/json")
        self.assertEqual(res.status_code, 200)
        self.assertTrue(res.json()['sent'])
        self.assertIn('report_link', res.json())

        report_res = self.client.get(res.json()['report_link'],
                                     **self.user_auth_headers,
                                     content_type="application/json")
        self.assertEqual(report_res.status_code, 200)
        self.assertEqual(report_res.json()['problem_details'],
                         self.contact_mechanic_request_body['problem_details'])
        self.assertEqual(report_res.json()['mechanic']['mechanic_code'],
                         self.contact_mechanic_request_body['mechanic_code'])
        self.assertEqual(report_res.json()['vehicle']['vin'],
                         self.contact_mechanic_request_body['vin'])

        service_requests = self.client.get(
            '/workshop/api/mechanic/service_requests',
            **self.mechanic_auth_headers,
            content_type="application/json")
        self.assertEqual(service_requests.json()['service_requests'][0],
                         report_res.json())
