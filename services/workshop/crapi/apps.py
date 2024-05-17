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
Configuration for crapi application
"""
import django
import sys
from django.apps import AppConfig
import bcrypt
from django.utils import timezone
from django.db import models
from django.db import connection, transaction
import logging
import traceback

logger = logging.getLogger()


def create_products():
    from crapi.shop.models import Product

    product_details_all = [
        {"name": "Seat", "price": 10, "image_url": "images/seat.svg"},
        {"name": "Wheel", "price": 10, "image_url": "images/wheel.svg"},
    ]
    for product_details in product_details_all:
        if Product.objects.filter(name=product_details["name"]).exists():
            logger.info("Product already exists. Skipping: " + product_details["name"])
            continue
        product = Product.objects.create(
            name=product_details["name"],
            price=float(product_details["price"]),
            image_url=product_details["image_url"],
        )
        product.save()
        logger.info("Created Product: " + str(product.__dict__))


def create_mechanics():
    from crapi.user.models import User, UserDetails
    from crapi.mechanic.models import Mechanic

    mechanic_details_all = [
        {
            "name": "Jhon",
            "email": "jhon@example.com",
            "number": "",
            "password": "Admin1@#",
            "mechanic_code": "TRAC_JHN",
        },
        {
            "name": "James",
            "email": "james@example.com",
            "number": "",
            "password": "Admin1@#",
            "mechanic_code": "TRAC_JME",
        },
    ]
    for mechanic_details in mechanic_details_all:
        uset = User.objects.filter(email=mechanic_details["email"])
        if not uset.exists():
            try:
                cursor = connection.cursor()
                cursor.execute("select nextval('user_login_id_seq')")
                result = cursor.fetchone()
                user_id = result[0]
            except Exception as e:
                logger.error("Failed to fetch user_login_id_seq" + str(e))
                user_id = 1

            user = User.objects.create(
                id=user_id,
                email=mechanic_details["email"],
                number=mechanic_details["number"],
                password=bcrypt.hashpw(
                    mechanic_details["password"].encode("utf-8"), bcrypt.gensalt()
                ).decode(),
                role=User.ROLE_CHOICES.MECH,
                created_on=timezone.now(),
            )
            user.save()
            logger.info("Created User: " + str(user.__dict__))
        else:
            user = uset.first()

        if Mechanic.objects.filter(mechanic_code=mechanic_details["mechanic_code"]):
            logger.info(
                "Mechanic already exists. Skipping: "
                + mechanic_details["mechanic_code"]
                + " "
                + mechanic_details["name"]
                + " "
                + mechanic_details["email"]
            )
            continue
        mechanic = Mechanic.objects.create(
            mechanic_code=mechanic_details["mechanic_code"], user=user
        )
        mechanic.save()
        try:
            cursor = connection.cursor()
            cursor.execute("select nextval('user_details_id_seq')")
            result = cursor.fetchone()
            user_details_id = result[0]
        except Exception as e:
            logger.error("Failed to fetch user_details_id_seq" + str(e))
            user_details_id = 1
        userdetails = UserDetails.objects.create(
            id=user_details_id,
            available_credit=0,
            name=mechanic_details["name"],
            status="ACTIVE",
            user=user,
        )
        userdetails.save()


def create_reports():
    import random
    import sys
    import textwrap
    from crapi.user.models import User, UserDetails, Vehicle
    from crapi.mechanic.models import Mechanic, ServiceRequest
    from django.utils import timezone

    count = ServiceRequest.objects.all().count()
    if count >= 5:
        return
    logger.info("Creating Reports")
    mechanics = Mechanic.objects.all()
    vehicles = Vehicle.objects.all()
    for i in range(5):
        try:
            mechanic = random.choice(mechanics)
            vehicle = random.choice(vehicles)
            status = random.choice(ServiceRequest.STATUS_CHOICES)[0]
            vehicle_model = vehicle.vehicle_model
            vehicle_company = vehicle_model.vehiclecompany
            user = vehicle.owner
            user_detail = UserDetails.objects.filter(user=user).first()
            service_request = ServiceRequest.objects.create(
                vehicle=vehicle,
                mechanic=mechanic,
                problem_details=textwrap.dedent(
                    """\
                    My car {} - {} is having issues.
                    Can you give me a call on my mobile {},
                    Or send me an email at {}
                    Thanks,
                    {}.
                    """.format(
                        vehicle_company.name,
                        vehicle_model.model,
                        user.number,
                        user.email,
                        user_detail.name,
                    )
                ),
                status=status,
                created_on=timezone.now(),
            )
            service_request.save()
            logger.info(
                "Created Service Request for User %s: %s",
                user.email,
                service_request.__dict__,
            )
        except Exception as e:
            print(sys.exc_info()[0])
            logger.error("Failed to create report: " + str(e))


def create_orders():
    import uuid
    from crapi.user.models import User, UserDetails
    from crapi.shop.models import Product
    from crapi.shop.models import Order

    if Order.objects.all().count() >= 1:
        return
    users = User.objects.filter(role=User.ROLE_CHOICES.PREDEFINED).order_by("id")
    for user in users:
        product = Product.objects.filter(name="Seat").first()
        order = Order.objects.create(
            user=user,
            product=product,
            quantity=2,
            created_on=timezone.now(),
            transaction_id=uuid.uuid4(),
        )
        order.save()
        logger.info("Created Order for User %s: %s", user.email, order.__dict__)


class CRAPIConfig(AppConfig):
    """
    Stores all meta data of crapi application
    """

    name = "crapi"

    def ready(self):
        """
        Pre-populate mechanic model and product model
        :return: None
        """
        # Check if sys.argv contains 'runserver' or 'runserver_plus'
        is_runserver = any("runserver" in x for x in sys.argv)
        if not is_runserver:
            return
        logger.info("Pre Populating Model Data")
        try:
            create_products()
        except Exception as e:
            logger.error("Cannot Pre Populate Products: " + str(e))
        try:
            create_mechanics()
        except Exception as e:
            logger.error("Cannot Pre Populate Mechanics: " + str(e))
        try:
            create_reports()
        except Exception as e:
            logger.error("Cannot Pre Populate Reports: " + str(e))
        try:
            create_orders()
        except Exception as e:
            logger.error("Cannot Pre Populate Orders: " + str(e))
