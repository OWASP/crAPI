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
Contains all the methods related to jwt token
"""
from datetime import datetime, timedelta
from functools import wraps
from rest_framework import status
from rest_framework.response import Response
import jwt
from django.conf import settings
from utils import messages
from user.models import User


def get_jwt(user, exp=None):
    """
    returns a jwt token from User object
    :param user: user object
    :param exp: expiry date and time for jwt
                if not mentioned, 3 hours from iat
    :return: jwt token as a string
    """
    pay_load = {
        'sub': user.email,
        'iat': datetime.now(),
        'exp': exp or (datetime.now() + timedelta(hours=3))
    }
    encoded = jwt.encode(pay_load, settings.JWT_SECRET, algorithm='HS512')
    return encoded.decode("utf-8")


def jwt_auth_required(func):
    """
    decorator for authorizing http requests
    :param func: view function of the api
    :return: a new view function which
        calls the actual view function if authorized
        returns error message if not authorized
    """
    @wraps(func)
    def new_func(*args, **kwargs):
        try:
            request = args[1]
            if 'HTTP_AUTHORIZATION' in request.META \
                    and request.META.get('HTTP_AUTHORIZATION')[0:7] == 'Bearer ':
                token = request.META.get('HTTP_AUTHORIZATION')[7:]
                decoded = jwt.decode(token, settings.JWT_SECRET, algorithms=['HS512'])
                username = decoded['sub']
                user = User.objects.get(email=username)
                kwargs['user'] = user  # Add user object to the view function if authorized
                return func(*args, **kwargs)
            return Response(
                {'message': messages.JWT_REQUIRED},
                status=status.HTTP_401_UNAUTHORIZED
            )
        except (jwt.exceptions.DecodeError, User.DoesNotExist):
            return Response(
                {'message': messages.INVALID_TOKEN},
                status=status.HTTP_401_UNAUTHORIZED
            )
        except jwt.exceptions.ExpiredSignatureError:
            return Response(
                {'message': messages.TOKEN_EXPIRED},
                status=status.HTTP_401_UNAUTHORIZED
            )
    return new_func
