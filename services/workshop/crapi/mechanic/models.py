"""
Models related to Mechanic
Mechanic and Service Request Models
"""
from django.db import models

from user.models import User, Vehicle
from collections import OrderedDict
from extended_choices import Choices

class Mechanic(models.Model):
    """
    Mechanic Model
    represents a mechanic for the application
    """

    mechanic_code = models.CharField(max_length=100, null=False, unique=True)
    user = models.ForeignKey(User, on_delete=models.CASCADE)

    class Meta:
        db_table = 'mechanic'

    def __str__(self):
        return f"<Mechanic: {self.mechanic_code}>"


class ServiceRequest(models.Model):
    """
    Service Request Model
    represents a service request in the application
    """

    mechanic = models.ForeignKey(Mechanic, on_delete=models.CASCADE)
    vehicle = models.ForeignKey(Vehicle, on_delete=models.CASCADE)
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
