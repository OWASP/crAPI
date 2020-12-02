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
