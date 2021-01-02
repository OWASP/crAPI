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
shop URL Configuration
The `urlpatterns` list routes URLs to views.
"""
from django.conf.urls import url

import crapi.shop.views as shop_views

urlpatterns = [
    # Do not change the order of URLs
    url(r'products$', shop_views.ProductView.as_view()),
    url(r'orders/all$', shop_views.OrderDetailsView.as_view()),
    url(r'orders/return_order$', shop_views.ReturnOrder.as_view()),
    url(r'orders/(?P<order_id>\d+)$', shop_views.OrderControlView.as_view()),
    url(r'orders$', shop_views.OrderControlView.as_view()),
    url(r'apply_coupon$', shop_views.ApplyCouponView.as_view()),
    url(r'return_qr_code$', shop_views.ReturnQRCodeView.as_view(), name="shop-return-qr-code"),
]
