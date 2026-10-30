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
Top-level URL configuration for the Workshop service.

crapi-web proxies /workshop/ here, and this file splits the API into the four
Django apps under crapi/, each with its own urls.py and views.py:

- api/mechanic/   mechanic signup/listing, service requests and reports
- api/merchant/   contact_mechanic (the SSRF endpoint) and user-side
                  service-request views
- api/shop/       products, orders, returns, coupons
- api/management/ admin user listing

The `urlpatterns` list routes URLs to URLConf.
"""
from django.urls import path, include

urlpatterns = [
    path("api/mechanic/", include("crapi.mechanic.urls")),
    path("api/merchant/", include("crapi.merchant.urls")),
    path("api/shop/", include("crapi.shop.urls")),
    path("api/management/", include("crapi.user.urls")),
]
