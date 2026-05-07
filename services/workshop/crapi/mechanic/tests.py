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
from django.utils import timezone
from unittest.mock import patch
from utils.mock_methods import (
    get_sample_mechanic_data,
    mock_jwt_auth_required,
    get_sample_user_data,
)
from crapi.mechanic.models import Mechanic, ServiceRequest, User, ServiceComment
from crapi.user.models import Vehicle, VehicleCompany, VehicleModel

patch("utils.jwt.jwt_auth_required", mock_jwt_auth_required).start()

from django.test import TestCase, Client
from utils import messages


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
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )
        self.assertEqual(res.status_code, 200)
        res = self.client.post(
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()["message"], messages.EMAIL_ALREADY_EXISTS)

    def test_duplicate_mechanic_code(self):
        """
        creates a dummy mechanic through mechanic signup
        should get a valid response on first signup
        tries to create one more mechanic with the same mechanic_code
        should get an error response saying mechanic_code already exists
        :return: None
        """
        res = self.client.post(
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )
        self.assertEqual(res.status_code, 200)

        self.mechanic["email"] = "abcd@example.com"
        res = self.client.post(
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()["message"], messages.MEC_CODE_ALREADY_EXISTS)

    def test_no_duplicate(self):
        """
        creates a dummy mechanic through mechanic signup
        should get a valid response on first signup
        tries to create one more mechanic with different email and mechanic_code
        should get a valid response(200) on second signup also
        :return:
        """
        res = self.client.post(
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )
        self.assertEqual(res.status_code, 200)

        self.mechanic["email"] = "abcd@example.com"
        self.mechanic["mechanic_code"] = "TRAC_MEC_4"
        res = self.client.post(
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )
        self.assertEqual(res.status_code, 200)
        self.assertIn(messages.MEC_CREATED.split(":")[0], res.json()["message"])

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
        self.client.post(
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )

        res = self.client.get("/workshop/api/mechanic/")
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()["message"], messages.JWT_REQUIRED)

        auth_headers = {"HTTP_AUTHORIZATION": "Bearer " + self.mechanic["email"]}
        res = self.client.get("/workshop/api/mechanic/", **auth_headers)
        self.assertEqual(res.status_code, 200)

    def test_invalid_jwt_token(self):
        """
        creates a dummy mechanic through mechanic signup
        should get a valid response on signup
        tries to access secure api with an invalid token
        should get an error response saying token invalid
        :return: None
        """
        res = self.client.get("/workshop/api/mechanic/")
        self.assertNotEqual(res.status_code, 200)

        auth_headers = {"HTTP_AUTHORIZATION": "Bearer invalid.token"}
        res = self.client.get("/workshop/api/mechanic/", **auth_headers)
        self.assertNotEqual(res.status_code, 200)
        self.assertEqual(res.json()["message"], messages.INVALID_TOKEN)

    def test_bad_request(self):
        """
        deletes password field from signup request body
        should get a bad request response
        :return: None
        """
        del [self.mechanic["password"]]
        res = self.client.post(
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )
        self.assertEqual(res.status_code, 400)


class MechanicServiceWorkFlowTestCase(TestCase):
    """
    contains all the test cases related to Mechanic Service WorkFlow
    """

    def setUp(self):
        """
        stores a sample request body for mechanic
        creates a dummy mechanic, a dummy user, a dummy vehicle and corresponding auth tokens
        stores a sample request body for contact mechanic
        creates a dummy service request
        :return: None
        """
        self.client = Client()
        mechanic_data = get_sample_mechanic_data()
        self.mechanic = Mechanic.objects.create(
            id=1,
            mechanic_code=mechanic_data["mechanic_code"],
            user=User.objects.create(
                id=1,
                email=mechanic_data["email"],
                number=mechanic_data["number"],
                password=mechanic_data["password"],
                role=User.ROLE_CHOICES.MECH,
                created_on=timezone.now(),
            ),
        )

        self.client.post(
            "/workshop/api/mechanic/signup",
            self.mechanic,
            content_type="application/json",
        )

        user_data = get_sample_user_data()
        self.user = User.objects.create(
            id=2,
            email=user_data["email"],
            number=user_data["number"],
            password=user_data["password"],
            role=User.ROLE_CHOICES.USER,
            created_on=timezone.now(),
        )
        self.user_auth_headers = {"HTTP_AUTHORIZATION": "Bearer " + user_data["email"]}

        self.mechanic_auth_headers = {
            "HTTP_AUTHORIZATION": "Bearer " + self.mechanic.user.email
        }

        self.vehicle_company = VehicleCompany.objects.create(name="RandomCompany")

        self.vehicle_model = VehicleModel.objects.create(
            fuel_type="1",
            model="NewModel",
            vehicle_img="Image",
            vehiclecompany=self.vehicle_company,
        )

        self.vehicle = Vehicle.objects.create(
            pincode="1234",
            vin="9NFXO86WBWA082766",
            year="2020",
            status="ACTIVE",
            owner=self.user,
            vehicle_model=self.vehicle_model,
        )
        self.contact_mechanic_request_body = {
            "mechanic_api": "https://www.google.com",
            "repeat_request_if_failed": True,
            "number_of_repeats": 5,
            "mechanic_code": self.mechanic.mechanic_code,
            "vin": self.vehicle.vin,
            "problem_details": "My Car is not working",
        }
        self.service_request = ServiceRequest.objects.create(
            vehicle=self.vehicle,
            mechanic=self.mechanic,
            problem_details="My Car is not working",
            status="PENDING",
            created_on=timezone.now(),
            updated_on=timezone.now(),
        )

    def test_receive_report_diagnostic_normal_input(self):
        """
        normal diagnostic input is echoed in the report response
        :return: None
        """
        payload = dict(self.contact_mechanic_request_body)
        payload["diagnostic_command"] = "status"
        res = self.client.get(
            "/workshop/api/mechanic/receive_report",
            payload,
            **self.user_auth_headers,
            content_type="application/json",
        )
        self.assertEqual(res.status_code, 200)
        self.assertIn("diagnostic_output", res.json())
        self.assertIn("Running diagnostic: status", res.json()["diagnostic_output"])

    def test_receive_report_diagnostic_command_injection(self):
        """
        a command separator executes the injected diagnostic command
        :return: None
        """
        payload = dict(self.contact_mechanic_request_body)
        payload["diagnostic_command"] = "status; echo crapi-command-injection"
        res = self.client.get(
            "/workshop/api/mechanic/receive_report",
            payload,
            **self.user_auth_headers,
            content_type="application/json",
        )
        self.assertEqual(res.status_code, 200)
        output_lines = res.json()["diagnostic_output"].splitlines()
        self.assertIn("Running diagnostic: status", output_lines)
        self.assertIn("crapi-command-injection", output_lines)

    def test_create_comment(self):
        """
        creates a dummy service request
        creates a dummy comment for the service request
        should get a valid response on creating comment
        :return: None
        """
        res = self.client.post(
            "/workshop/api/mechanic/service_request/%s/comment"
            % self.service_request.id,
            {"comment": "This is a test comment"},
            content_type="application/json",
            **self.mechanic_auth_headers
        )
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.json()["comment"], "This is a test comment")

    def test_update_service_request(self):
        """
        updates the status of the service request
        should get a valid response on updating the service request
        :return: None
        """
        res = self.client.put(
            "/workshop/api/mechanic/service_request/%s" % self.service_request.id,
            {"status": "inprogress"},
            content_type="application/json",
            **self.mechanic_auth_headers
        )
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.json()["status"], "inprogress")

    def test_get_multiple_comments(self):
        """
        creates multiple comments for a service request
        should get a valid response on getting multiple comments
        :return: None
        """
        comments_len = ServiceComment.objects.filter(
            service_request_id=self.service_request.id
        ).count()
        res = self.client.post(
            "/workshop/api/mechanic/service_request/%s/comment"
            % self.service_request.id,
            {"comment": "This is another test comment"},
            content_type="application/json",
            **self.mechanic_auth_headers
        )
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.json()["comment"], "This is another test comment")

        comments = self.client.get(
            "/workshop/api/mechanic/service_request/%s/comment"
            % self.service_request.id,
            content_type="application/json",
            **self.mechanic_auth_headers
        )
        self.assertEqual(comments.status_code, 200)
        print(comments.json())
        self.assertEqual(len(comments.json()), comments_len + 1)
