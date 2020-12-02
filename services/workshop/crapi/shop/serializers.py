"""
contains all serializers related to Shop APIs
"""
from rest_framework import serializers

from crapi.shop.models import Order, Product, Coupon
from user.serializers import UserSerializer


class ProductSerializer(serializers.ModelSerializer):
    """
    Serializer for Product model
    """

    class Meta:
        """
        Meta class for ProductSerializer
        """
        model = Product
        fields = ('id', 'name', 'price', 'image_url')


class OrderSerializer(serializers.ModelSerializer):
    """
    Serializer for Order model
    """
    user = UserSerializer()
    product = ProductSerializer()

    class Meta:
        """
        Meta class for OrderSerializer
        """
        model = Order
        fields = ('id', 'user', 'product', 'quantity', 'status', 'created_on')


class CouponSerializer(serializers.Serializer):
    """
    Serializer for Coupon model
    """
    coupon_code = serializers.CharField()
    amount = serializers.IntegerField()
    class Meta:
        """
        Meta class for CouponSerializer
        """
        model = Coupon


class ProductQuantitySerializer(serializers.Serializer):
    """
    Serializer for Product order API
    """
    product_id = serializers.IntegerField()
    quantity = serializers.IntegerField()
