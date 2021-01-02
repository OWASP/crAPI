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
