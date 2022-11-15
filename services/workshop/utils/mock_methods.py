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

import logging
import jwt
from functools import wraps
from rest_framework import status
from rest_framework.response import Response
from utils import messages
from user.models import User

"""
contains all methods that are common and
which can be used during testing
"""

logger = logging.getLogger()

def get_sample_mechanic_data():
    """
    gives mechanic data which can be used for testing
    :return: sample mechanic object
    """
    return {
        "name": "MechRaju",
        "email": "mechraju@crapi.com",
        "mechanic_code": "TRAC_MEC_3",
        "number": "9123456708",
        "password": "admin",
    }

def get_sample_user_data():
    """
    gives user data which can be used for testing
    :return: sample user object
    """
    return {
        "name": "TestUser",
        "email": "test@crapi.com",
        "number": "9123456708",
        "password": "password",
    }

def mock_jwt_auth_required(func):
    """
    mock function to validate jwt
    :param func: view function of the api
    :return: a new view function which
        calls the actual view function if token is a valid email
        returns error message if token is a invalid email
    """
    @wraps(func)
    def new_func(*args, **kwargs):
        try:
            request = args[1]
            if 'HTTP_AUTHORIZATION' in request.META \
                    and request.META.get('HTTP_AUTHORIZATION')[0:7] == 'Bearer ':
                token = request.META.get('HTTP_AUTHORIZATION')[7:]
                user = User.objects.get(email=token)
                # Add user object to the view function if authorized
                kwargs['user'] = user
                return func(*args, **kwargs)

            return Response({'message': messages.JWT_REQUIRED},
                            status=status.HTTP_401_UNAUTHORIZED,
                            content_type="application/json")

        except (jwt.exceptions.DecodeError, User.DoesNotExist):
            return Response({'message': messages.INVALID_TOKEN},
                            status=status.HTTP_401_UNAUTHORIZED,
                            content_type="application/json")

    return new_func
