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
