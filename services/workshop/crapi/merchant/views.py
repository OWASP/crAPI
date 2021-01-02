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
contains all the views related to Merchant
"""
import logging
import requests
from requests.exceptions import MissingSchema, InvalidURL
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from crapi.merchant.serializers import ContactMechanicSerializer
from utils.jwt import jwt_auth_required
from utils import messages
from utils.logging import log_error

logger = logging.getLogger()


class ContactMechanicView(APIView):
    """
    View for contact mechanic feature
    """
    @jwt_auth_required
    def post(self, request, user=None):
        """
        contact_mechanic view to call the mechanic api
        :param request: http request for the view
            method allowed: POST
            http request should be authorised by the jwt token of the user
            mandatory fields: ['mechanic_api', 'repeat_request_if_failed', 'number_of_repeats']
        :param user: User object of the requesting user
        :returns Response object with
            response_from_mechanic_api and 200 status if no error
            message and corresponding status if error
        """
        request_data = request.data
        serializer = ContactMechanicSerializer(data=request_data)
        if not serializer.is_valid():
            log_error(request.path, request.data, status.HTTP_400_BAD_REQUEST, serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        repeat_request_if_failed = request_data['repeat_request_if_failed']
        number_of_repeats = request_data['number_of_repeats']
        if repeat_request_if_failed and number_of_repeats < 1:
            return Response(
                {'message': messages.MIN_NO_OF_REPEATS_FAILED},
                status=status.HTTP_503_SERVICE_UNAVAILABLE
            )
        elif repeat_request_if_failed and number_of_repeats > 100:
            return Response(
                {'message': messages.NO_OF_REPEATS_EXCEEDED},
                status=status.HTTP_503_SERVICE_UNAVAILABLE
            )

        repeat_count = 0
        while True:
            logger.info(f"Repeat count: {repeat_count}")
            try:
                mechanic_response = requests.get(
                    url=request_data['mechanic_api'],
                    params=request_data,
                    headers={'Authorization': request.META.get('HTTP_AUTHORIZATION')}
                )
                if mechanic_response.status_code == status.HTTP_200_OK:
                    logger.info(f"Got a valid response at repeat count: {repeat_count}")
                    break
                if not repeat_request_if_failed:
                    break
                if repeat_count == number_of_repeats:
                    break
                repeat_count += 1
            except (MissingSchema, InvalidURL) as e:
                log_error(request.path, request.data, status.HTTP_400_BAD_REQUEST, e)
                return Response({'message': str(e)}, status=status.HTTP_400_BAD_REQUEST)
            except requests.exceptions.ConnectionError as e:
                if not repeat_request_if_failed:
                    return Response(
                        {'message': messages.COULD_NOT_CONNECT},
                        status=status.HTTP_400_BAD_REQUEST
                    )
                if repeat_count == number_of_repeats:
                    return Response(
                        {'message': messages.COULD_NOT_CONNECT},
                        status=status.HTTP_400_BAD_REQUEST
                    )
                repeat_count += 1
                continue
        mechanic_response_status = mechanic_response.status_code
        try:
            mechanic_response = mechanic_response.json()
        except ValueError:
            mechanic_response = mechanic_response.text
        return Response({
            'response_from_mechanic_api': mechanic_response,
            'status': mechanic_response_status
        }, status=mechanic_response_status)
