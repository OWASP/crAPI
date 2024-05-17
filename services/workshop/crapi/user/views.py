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
from crapi.user.serializers import UserDetailsSerializer
from crapi.user.models import User, UserDetails
from crapi_site import settings
from utils.jwt import jwt_auth_required
from utils import messages
from utils.logging import log_error
from rest_framework.pagination import LimitOffsetPagination

logger = logging.getLogger()


class AdminUserView(APIView, LimitOffsetPagination):
    """
    View for admin user to fetch user details
    """

    @jwt_auth_required
    def get(self, request, user=None):
        """
        Admin user view to fetch user details
        :param request: http request for the view
            method allowed: GET
            http request should be authorised by the jwt token of the user
            mandatory fields: []
        :returns Response object with
            user details and 200 status if no error
            message and corresponding status if error
        """
        # Sort by id
        userdetails = UserDetails.objects.all().order_by("id")
        if not userdetails:
            return Response(
                {"message": messages.NO_USER_DETAILS}, status=status.HTTP_404_NOT_FOUND
            )
        paginated = self.paginate_queryset(userdetails, request)
        serializer = UserDetailsSerializer(paginated, many=True)
        response_data = dict(
            users=serializer.data,
            next_offset=(
                self.offset + self.limit
                if self.offset + self.limit < self.count
                else None
            ),
            previous_offset=(
                self.offset - self.limit if self.offset - self.limit >= 0 else None
            ),
            count=self.get_count(paginated),
        )
        return Response(response_data, status=status.HTTP_200_OK)
