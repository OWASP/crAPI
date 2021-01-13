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
Models related to Mechanic
Mechanic and Service Request Models
"""
from django.db import models

from user.models import User, Vehicle
from collections import OrderedDict
from extended_choices import Choices
from django_db_cascade.fields import ForeignKey, OneToOneField
from django_db_cascade.deletions import DB_CASCADE

class Mechanic(models.Model):
    """
    Mechanic Model
    represents a mechanic for the application
    """

    mechanic_code = models.CharField(max_length=100, null=False, unique=True)
    user = ForeignKey(User, DB_CASCADE)

    class Meta:
        db_table = 'mechanic'

    def __str__(self):
        return f"<Mechanic: {self.mechanic_code}>"


class ServiceRequest(models.Model):
    """
    Service Request Model
    represents a service request in the application
    """

    mechanic = ForeignKey(Mechanic, DB_CASCADE)
    vehicle = ForeignKey(Vehicle, DB_CASCADE)
    problem_details = models.CharField(max_length=500, blank=True)
    created_on = models.DateTimeField()
    updated_on = models.DateTimeField(null=True)

    STATUS_CHOICES = Choices(
        ('PEN', "Pending", "Pending"),
        ('FIN', "Finished", "Finished")
    )
    status = models.CharField(max_length=10, choices=STATUS_CHOICES, default=STATUS_CHOICES.PEN)

    class Meta:
        db_table = 'service_request'

    def __str__(self):
        return f'<ServiceRequest: {self.id}>'
