"""
crAPI URL Configuration
The `urlpatterns` list routes URLs to URLConf.
"""
from django.urls import path, include

urlpatterns = [
    path('api/mechanic/', include('crapi.mechanic.urls')),
    path('api/merchant/', include('crapi.merchant.urls')),
    path('api/shop/', include('crapi.shop.urls')),
]
