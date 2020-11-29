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
