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
contains all the serializers for User and Vehicle Objects
"""
from rest_framework import serializers

from user.models import User, Vehicle


class UserSerializer(serializers.ModelSerializer):
    """
    Serializer for User model
    """

    class Meta:
        """
        Meta class for UserSerializer
        """
        model = User
        fields = ('email', 'number')


class VehicleSerializer(serializers.ModelSerializer):
    """
    Serializer for Vehicle model
    """
    owner = UserSerializer()

    class Meta:
        """
        Meta class for MechanicSerializer
        """
        model = Vehicle
        fields = ('id', 'vin', 'owner')
