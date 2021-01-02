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
contains all the serializers for mechanic APIs
"""
from rest_framework import serializers

from crapi.mechanic.models import Mechanic, ServiceRequest
from user.serializers import UserSerializer, VehicleSerializer


class MechanicSerializer(serializers.ModelSerializer):
    """
    Serializer for Mechanic model
    """
    user = UserSerializer()

    class Meta:
        """
        Meta class for MechanicSerializer
        """
        model = Mechanic
        fields = ('id', 'mechanic_code', 'user')


class ServiceRequestSerializer(serializers.ModelSerializer):
    """
    Serializer for Mechanic model
    """
    mechanic = MechanicSerializer()
    vehicle = VehicleSerializer()
    created_on = serializers.DateTimeField(format="%d %B, %Y, %H:%M:%S")

    class Meta:
        """
        Meta class for ServiceRequestSerializer
        """
        model = ServiceRequest
        fields = ('id', 'mechanic', 'vehicle', 'problem_details', 'status', 'created_on')


class ReceiveReportSerializer(serializers.Serializer):
    """
    Serializer for Receive Report API
    """
    mechanic_code = serializers.CharField()
    problem_details= serializers.CharField()
    vin = serializers.CharField()
    owner_id = serializers.CharField(required=False)


class SignUpSerializer(serializers.Serializer):
    """
    Serializer for Sign up
    """
    name = serializers.CharField()
    email = serializers.EmailField()
    number = serializers.CharField()
    password = serializers.CharField()
    mechanic_code = serializers.CharField()
