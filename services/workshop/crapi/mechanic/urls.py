"""
mechanic URL Configuration
The `urlpatterns` list routes URLs to views.
"""
from django.conf.urls import url

import crapi.mechanic.views as mechanic_views

urlpatterns = [
    url(r'signup$', mechanic_views.SignUpView.as_view()),
    url(r'receive_report$', mechanic_views.ReceiveReportView.as_view()),
    url(r'mechanic_report$', mechanic_views.GetReportView.as_view(), name="get-mechanic-report"),
    url(r'service_requests$', mechanic_views.ServiceRequestsView.as_view()),
    url(r'$', mechanic_views.MechanicView.as_view()),
]
