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
contains serializers for Merchant APIs
"""
from rest_framework import serializers


class ContactMechanicSerializer(serializers.Serializer):
    """
    Serializer for Contact Mechanic model.
    """
    mechanic_api = serializers.CharField()
    repeat_request_if_failed = serializers.BooleanField()
    number_of_repeats = serializers.IntegerField()
