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
