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
