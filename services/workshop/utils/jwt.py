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
import requests
import jwt
from functools import wraps
from rest_framework import status
from rest_framework.response import Response
from django.conf import settings
from utils import messages
from crapi.user.models import User
import urllib3
import logging

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

logger = logging.getLogger()


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
            if (
                "HTTP_AUTHORIZATION" in request.META
                and request.META.get("HTTP_AUTHORIZATION")[0:7] == "Bearer "
            ):
                token = request.META.get("HTTP_AUTHORIZATION")[7:]
                tokenJson = {"token": token}
                identity_url = settings.IDENTITY_VERIFY
                logger.debug(f"Identity url: {identity_url}, tokenJson: {tokenJson}")
                token_verify_response = requests.post(
                    identity_url, json=tokenJson, verify=False
                )
                logger.debug(
                    f"Identity url: {identity_url}, token_verify_response: {token_verify_response}"
                )
                response_status_code = token_verify_response.status_code
                if response_status_code == status.HTTP_200_OK:
                    decoded = jwt.decode(token, options={"verify_signature": False})
                    username = decoded["sub"]
                    user = User.objects.get(email=username)
                    # Add user object to the view function if authorized
                    kwargs["user"] = user
                    return func(*args, **kwargs)
                logger.debug("JWT token verification failed")
                return Response(
                    {"message": messages.INVALID_TOKEN},
                    status=status.HTTP_401_UNAUTHORIZED,
                    content_type="application/json",
                )
            logger.debug("JWT token not found in request header")
            return Response(
                {"message": messages.JWT_REQUIRED},
                status=status.HTTP_401_UNAUTHORIZED,
                content_type="application/json",
            )

        except (jwt.exceptions.DecodeError, User.DoesNotExist) as e:
            logger.debug(
                f"JWT token verification failed with exception: {e}", exc_info=True
            )
            return Response(
                {"message": messages.INVALID_TOKEN},
                status=status.HTTP_401_UNAUTHORIZED,
                content_type="application/json",
            )

    return new_func
