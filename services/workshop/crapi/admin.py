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
Register the models here to view the same in Django Admin.
"""
from django.contrib import admin

from crapi.mechanic.models import ServiceRequest, Mechanic
from crapi.shop.models import Order, Product, AppliedCoupon
from user.models import User, UserDetails, Vehicle

admin.site.register(Order)
admin.site.register(User)
admin.site.register(Product)
admin.site.register(AppliedCoupon)
admin.site.register(Mechanic)
admin.site.register(ServiceRequest)
admin.site.register(UserDetails)
admin.site.register(Vehicle)
