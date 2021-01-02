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
