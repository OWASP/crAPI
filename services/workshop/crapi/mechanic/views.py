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
contains all the views related to Mechanic
"""
import bcrypt
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt
from django.urls import reverse
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from django.db import models
from utils.jwt import jwt_auth_required
from utils import messages
from user.models import User, Vehicle, UserDetails
from utils.logging import log_error
from .models import Mechanic, ServiceRequest
from .serializers import MechanicSerializer, ServiceRequestSerializer, ReceiveReportSerializer, SignUpSerializer


class SignUpView(APIView):
    """
    Used to add a new mechanic
    """
    @csrf_exempt
    def post(self, request):
        """
        creates a new Mechanic in the db
        :param request: http request for the view
            method allowed: POST
            mandatory fields: ['name', 'email', 'number', 'password', 'mechanic_code']
        :returns Response object with
            mechanics list and 200 status if no error
            message and corresponding status if error
        """
        serializer = SignUpSerializer(data=request.data)
        if not serializer.is_valid():
            log_error(request.path, request.data, status.HTTP_400_BAD_REQUEST, serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        mechanic_details = serializer.data

        if User.objects.filter(email=mechanic_details['email']).exists():
            return Response({'message': messages.EMAIL_ALREADY_EXISTS}, status=status.HTTP_400_BAD_REQUEST)
        if Mechanic.objects.filter(mechanic_code=mechanic_details['mechanic_code']).exists():
            return Response({'message': messages.MEC_CODE_ALREADY_EXISTS}, status=status.HTTP_400_BAD_REQUEST)
        try:
            user_id = User.objects.aggregate(models.Max('id'))['id__max'] + 1
        except TypeError:
            user_id = 1

        user = User.objects.create(
            id=user_id,
            email=mechanic_details['email'],
            number=mechanic_details['number'],
            password=bcrypt.hashpw(
                mechanic_details['password'].encode('utf-8'),
                bcrypt.gensalt()
            ).decode(),
            role=User.ROLE_CHOICES.MECH,
            created_on=timezone.now()
        )
        Mechanic.objects.create(
            mechanic_code=mechanic_details['mechanic_code'],
            user=user
        )
        try:
            user_details_id = UserDetails.objects.aggregate(models.Max('id'))['id__max'] + 1
        except TypeError:
            user_details_id = 1
        UserDetails.objects.create(
            id=user_details_id,
            available_credit=0,
            name=mechanic_details['name'],
            status='ACTIVE',
            user=user
        )
        return Response({'message': messages.MEC_CREATED.format(user.email)}, status=status.HTTP_200_OK)


class MechanicView(APIView):
    """
    Mechanic view to fetch all the mechanics
    """
    @jwt_auth_required
    def get(self, request, user=None):
        """
        get_mechanic view for fetching the list of mechanics
        :param request: http request for the view
            method allowed: GET
            http request should be authorised by the jwt token of the user
        :param user: User object of the requesting user
        :returns Response object with
            mechanics list and 200 status if no error
            message and corresponding status if error
        """
        mechanics = Mechanic.objects.all()
        serializer = MechanicSerializer(mechanics, many=True)
        response_data = dict(
            mechanics=serializer.data
        )
        return Response(response_data, status=status.HTTP_200_OK)


class ReceiveReportView(APIView):
    """
    View to receive report from contact mechanic feature
    """
    def get(self, request):
        """
        receive_report endpoint for mechanic
        :param request: http request for the view
            method allowed: POST
            mandatory fields: ['mechanic_code', 'problem_details', 'vin']
        :returns Response object with
            { service request id, report link } and 200 status if no error
            message and corresponding status if error
        """
        serializer = ReceiveReportSerializer(data=request.GET)
        if not serializer.is_valid():
            log_error(request.path, request.data, status.HTTP_400_BAD_REQUEST, serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        report_details = serializer.data
        mechanic = Mechanic.objects.get(mechanic_code=report_details['mechanic_code'])
        vehicle = Vehicle.objects.get(vin=report_details['vin'])
        service_request = ServiceRequest.objects.create(
            vehicle=vehicle,
            mechanic=mechanic,
            problem_details=report_details['problem_details'],
            created_on=timezone.now()
        )
        service_request.save()
        report_link = "{}?report_id={}".format(reverse("get-mechanic-report"), service_request.id)
        report_link = request.build_absolute_uri(report_link)
        return Response({
            'id': service_request.id,
            'sent': True,
            'report_link': report_link
        }, status=status.HTTP_200_OK)


class GetReportView(APIView):
    """
    View to get only particular service request
    """
    def get(self, request):
        """
        fetch service request details from report_link
        :param request: http request for the view
            method allowed: GET
        :returns Response object with
            service request object and 200 status if no error
            message and corresponding status if error
        """
        report_id = request.GET['report_id']
        if not report_id:
            return Response(
                {'message': messages.REPORT_ID_MISSING},
                status=status.HTTP_400_BAD_REQUEST
            )

        if not report_id.isnumeric():
            return Response(
                {'message': messages.INVALID_REPORT_ID},
                status=status.HTTP_400_BAD_REQUEST
            )
        service_request = ServiceRequest.objects.filter(id=report_id).first()
        if not service_request:
            return Response(
                {'message': messages.REPORT_DOES_NOT_EXIST},
                status=status.HTTP_400_BAD_REQUEST
            )
        serializer = ServiceRequestSerializer(service_request)
        response_data = dict(serializer.data)
        return Response(response_data, status=status.HTTP_200_OK)


class ServiceRequestsView(APIView):
    """
    View to return all the service requests
    """
    @jwt_auth_required
    def get(self, request, user=None):
        """
        fetch all service requests assigned to the particular mechanic
        :param request: http request for the view
            method allowed: GET
            http request should be authorised by the jwt token of the mechanic
        :param user: User object of the requesting user
        :returns Response object with
            list of service request object and 200 status if no error
            message and corresponding status if error
        """
        service_requests = ServiceRequest.objects.filter(mechanic__user=user)
        serializer = ServiceRequestSerializer(service_requests, many=True)
        response_data = dict(
            service_requests=serializer.data
        )
        return Response(response_data, status=status.HTTP_200_OK)
