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
contains views related to Shop APIs
"""
import logging
from django.utils import timezone
from django.http import FileResponse
from django.urls import reverse
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from crapi.shop.serializers import OrderSerializer, ProductSerializer, CouponSerializer, ProductQuantitySerializer
from utils.jwt import jwt_auth_required
from utils import messages
from crapi.shop.models import Order, Product, AppliedCoupon, Coupon
from user.models import UserDetails
from utils.logging import log_error
from django.core.exceptions import ObjectDoesNotExist

class ProductView(APIView):
    """
    Product Controller View
    """
    @jwt_auth_required
    def get(self, request, user):
        """
        products view for fetching the list of products
        :param request: http request for the view
            method allowed: GET
            http request should be authorised by the jwt token of the user
        :param user: User object of the requesting user
        :returns Response object with
            products list and 200 status if no error
            message and corresponding status if error
        """
        user_details = UserDetails.objects.get(user=user)
        products = Product.objects.all()
        serializer = ProductSerializer(products, many=True)
        response_data = dict(
            products=serializer.data,
            credit=user_details.available_credit,
        )
        return Response(response_data, status=status.HTTP_200_OK)

    @jwt_auth_required
    def post(self, request, user):
        """
        products view for adding a new product
        :param request: http request for the view
            method allowed: POST
            http request should be authorised by the jwt token of the user
            mandatory fields for POST and PUT http methods: ['name', 'price', 'image_url']
        :param user: User object of the requesting user
        :returns Response object with
            products list and 200 status if no error
            message and corresponding status if error
        """
        user_request_body = request.data
        serializer = ProductSerializer(data=user_request_body)
        if not serializer.is_valid():
            log_error(request.path, request.data, 400, serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save()
        return Response(serializer.data, status=status.HTTP_200_OK)


class OrderControlView(APIView):
    """
    Order Controller View
    """
    @jwt_auth_required
    def get(self, request, order_id=None, user=None):
        """
        order view for fetching  a particular order
        :param request: http request for the view
            method allowed: GET
            http request should be authorised by the jwt token of the user
        :param order_id:
            order_id of the order referring to\
        :param user: User object of the requesting user
        :returns Response object with
            order object and 200 status if no error
            message and corresponding status if error
        """
        order = Order.objects.get(id=order_id)
        if user != order.user:
            return Response({'message': messages.RESTRICTED}, status=status.HTTP_403_FORBIDDEN)
        serializer = OrderSerializer(order)
        response_data = dict(
            orders=serializer.data
        )
        return Response(response_data, status=status.HTTP_200_OK)

    @jwt_auth_required
    def post(self, request, order_id=None, user=None):
        """
        order view for adding a new order
        :param request: http request for the view
            method allowed: POST
            http request should be authorised by the jwt token of the user
            mandatory fields: ['product_id', 'quantity']
        :param order_id:
            order_id of the order referring to
            mandatory for GET and PUT http methods
        :param user: User object of the requesting user
        :returns Response object with
            order object and 200 status if no error
            message and corresponding status if error
        """
        request_data = request.data
        serializer = ProductQuantitySerializer(data=request_data)
        if not serializer.is_valid():
            log_error(request.path, request.data, status.HTTP_400_BAD_REQUEST, serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        product = Product.objects.get(id=request_data['product_id'])
        user_details = UserDetails.objects.get(user=user)
        if user_details.available_credit < product.price:
            return Response(
                {'message': messages.INSUFFICIENT_BALANCE},
                status=status.HTTP_400_BAD_REQUEST
            )
        user_details.available_credit -= float(product.price * request_data['quantity'])
        order = Order.objects.create(
            user=user,
            product=product,
            quantity=request_data['quantity'],
            created_on=timezone.now(),
        )
        user_details.save()
        return Response({
            'id': order.id,
            'message': messages.ORDER_CREATED,
            'credit': user_details.available_credit,
        }, status=status.HTTP_200_OK)

    @jwt_auth_required
    def put(self, request, order_id=None, user=None):
        """
        order view for updating a particular order
        :param request: http request for the view
            method allowed: PUT
            http request should be authorised by the jwt token of the user
            mandatory fields for POST and PUT http methods: ['product_id', 'quantity']
        :param order_id:
            order_id of the order referring to
            mandatory for GET and PUT http methods
        :param user: User object of the requesting user
        :returns Response object with
            order object and 200 status if no error
            message and corresponding status if error
        """
        request_data = request.data
        order = Order.objects.get(id=order_id)
        if user != order.user:
            return Response({'message': messages.RESTRICTED}, status=status.HTTP_403_FORBIDDEN)
        if 'quantity' in request_data:
            order.quantity = request_data['quantity']
        if 'status' in request_data and not Order.STATUS_CHOICES.has_value(request_data['status']):
            return Response(
                {'message': messages.INVALID_STATUS},
                status=status.HTTP_400_BAD_REQUEST
            )
        user_details = UserDetails.objects.get(user=order.user)
        if 'status' in request_data and request_data['status'] != order.status:
            order.status = request_data['status']
            if request_data['status'] == Order.STATUS_CHOICES.RETURNED.value:
                user_details.available_credit += float(order.quantity * order.product.price)
                user_details.save()
        order.save()
        serializer = OrderSerializer(order)
        response_data = dict(
            orders=serializer.data
        )
        return Response(response_data, status=status.HTTP_200_OK)


class OrderDetailsView(APIView):
    """
    Get the details of the orders.
    """

    @jwt_auth_required
    def get(self, request, user=None):
        """
        returns all the order of the particular user
        :param request: http request for the view
            method allowed: GET
            http request should be authorised by the jwt token of the user
        :param user: User object of the requesting user
        :returns Response object with
            list of order object and 200 status if no error
            message and corresponding status if error
        """
        orders = Order.objects.filter(user=user)
        serializer = OrderSerializer(orders, many=True)
        response_data = dict(
            orders=serializer.data
        )
        return Response(response_data, status=status.HTTP_200_OK)


class ReturnOrder(APIView):
    """
    Return Order View
    """
    @jwt_auth_required
    def post(self, request, user=None):
        """
        api for returning an order
        :param request: http request for the view
            method allowed: POST
            http request should be authorised by the jwt token of the user
        :param user: User object of the requesting user
        :returns Response object with
            message and 200 status if no error
            message and corresponding status if error
        """
        order = Order.objects.get(id=request.GET['order_id'])
        if user != order.user:
            return Response({'message': messages.RESTRICTED}, status=status.HTTP_403_FORBIDDEN)
        if order.status == Order.STATUS_CHOICES.RETURNED.value:
            return Response(
                {'message': messages.ORDER_ALREADY_RETURNED},
                status=status.HTTP_400_BAD_REQUEST
            )
        elif order.status == Order.STATUS_CHOICES.RETURN_PENDING.value:
            return Response(
                {'message': messages.ORDER_RETURNED_PENDING},
                status=status.HTTP_400_BAD_REQUEST
            )

        qr_code_url = request.build_absolute_uri(reverse("shop-return-qr-code"))
        order.status = Order.STATUS_CHOICES.RETURN_PENDING.value
        order.save()
        serializer = OrderSerializer(order)
        return Response({
            'message': messages.ORDER_RETURNING,
            'qr_code_url': qr_code_url,
            'order': serializer.data
        }, status=status.HTTP_200_OK)


class ReturnQRCodeView(APIView):
    """
    QR code image view
    """
    def get(self, request):
        """
        returns a qr code image
        :param request: http request for the view
            method allowed: GET
        :return: FileResponse
        """
        img = open('utils/return-qr-code.png', 'rb')
        return FileResponse(img)


class ApplyCouponView(APIView):
    """
    Apply Coupon View to increase the available credit
    """
    @jwt_auth_required
    def post(self, request, user=None):
        """
        api for checking if coupon is already claimed
        if claimed before: returns an error message
        else: increases the user credit
        :param request: http request for the view
            method allowed: POST
            http request should be authorised by the jwt token of the user
        :param user: User object of the requesting user
        :returns Response object with
            message and 200 status if no error
            message and corresponding status if error
        """
        coupon_request_body = request.data

        serializer = CouponSerializer(data=coupon_request_body)
        if not serializer.is_valid():
            log_error(request.path, request.data, 400, serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        try:
            coupon = Coupon.objects.using('mongodb').get(coupon_code=coupon_request_body['coupon_code'])
        except ObjectDoesNotExist as e:
            log_error(request.path, request.data, 400, e)
            return Response({'message': "Coupon not found"}, status=status.HTTP_400_BAD_REQUEST)
        if AppliedCoupon.objects.filter(user=user, coupon_code=coupon_request_body['coupon_code']).first():
            return Response(
                {'message': messages.COUPON_ALREADY_APPLIED},
                status=status.HTTP_400_BAD_REQUEST
            )
        AppliedCoupon.objects.create(
            user=user,
            coupon_code=coupon_request_body['coupon_code']
        )
        user_details = UserDetails.objects.get(user=user)
        user_details.available_credit += coupon_request_body['amount']
        user_details.save()
        return Response({
            'credit': user_details.available_credit,
            'message': messages.COUPON_APPLIED
        }, status=status.HTTP_200_OK)
