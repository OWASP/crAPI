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
Models related to User
These tables are created by user microservices
So fake migrations are performed on these models
"""
from django.db import models
from collections import OrderedDict
from extended_choices import Choices

class User(models.Model):
    """
    User Model
    represents an user for the application
    """

    id = models.AutoField(primary_key=True)
    created_on = models.DateTimeField()
    email = models.CharField(max_length=255, unique=True)
    jwt_token = models.CharField(max_length=500, unique=True, null=True)
    number = models.CharField(max_length=255, null=True)
    password = models.CharField(max_length=255)

    ROLE_CHOICES = Choices(
        ('USER', 1, 'User'),
        ('MECH', 2, 'Mechanic'),
        dict_class = OrderedDict
    )
    role = models.IntegerField(choices=ROLE_CHOICES, default=ROLE_CHOICES.USER)

    class Meta:
        db_table = 'user_login'
        managed = False

    def __str__(self):
        return f"<User: {self.email}>"


class UserDetails(models.Model):
    """
    UserDetails Model
    stores additional details for the user
    """
    id = models.AutoField(primary_key=True)
    available_credit = models.FloatField()
    name = models.CharField(max_length=255, null=True)
    status = models.CharField(max_length=255, null=True)
    user = models.ForeignKey(User, on_delete=models.CASCADE)

    class Meta:
        db_table = 'user_details'
        managed = False

    def __str__(self):
        return f"<UserDetails: {self.user.email}>"


class VehicleCompany(models.Model):
    """
    UserDetails Model
    stores additional details for the user
    """
    id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=255)

    class Meta:
        db_table = 'vehicle_company'
        managed = False

    def __str__(self):
        return f"<VehicleCompany: {self.name}>"


class VehicleModel(models.Model):
    """
    UserDetails Model
    stores additional details for the user
    """
    id = models.AutoField(primary_key=True)
    fuel_type = models.BigIntegerField()
    model = models.CharField(max_length=255)
    vehicle_img = models.CharField(max_length=255, null=True)
    vehiclecompany = models.ForeignKey(VehicleCompany, on_delete=models.CASCADE)

    class Meta:
        db_table = 'vehicle_model'
        managed = False

    def __str__(self):
        return f"<VehicleModel: {self.model}>"


class Vehicle(models.Model):
    """
    Vehicle Model
    represents a vehicle in the application
    """
    id = models.AutoField(primary_key=True)
    pincode = models.CharField(max_length=255, null=True)
    vin = models.CharField(max_length=255)
    year = models.BigIntegerField(null=True)
    vehicle_model = models.ForeignKey(VehicleModel, on_delete=models.CASCADE)
    owner = models.ForeignKey(User, on_delete=models.CASCADE)
    status = models.CharField(max_length=255)
    location_id = models.BigIntegerField(null=True)

    class Meta:
        db_table = 'vehicle_details'
        managed = False

    def __str__(self):
        return f"<Vehicle: {self.vin}>"
