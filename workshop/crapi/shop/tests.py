"""
contains all the test cases related to shop management
"""
from django.test import TestCase, Client

from utils.jwt import get_jwt
from utils.sample_data import get_sample_mechanic_data
from user.models import User


class ProductTestCase(TestCase):
    """
    contains all the test cases related to Products
    Attributes:
        client: Client object used for testing
        mechanic: sample mechanic sign up request body
        user: dummy mechanic object
        auth_headers: Auth headers for dummy mechanic
    """
    def setUp(self):
        """
        stores a sample request body for mechanic signup
        creates a dummy mechanic corresponding auth tokens
        :return: None
        """
        self.client = Client()
        self.mechanic = get_sample_mechanic_data()
        self.client.post('/api/mechanic/signup', self.mechanic, content_type="application/json")
        self.user = User.objects.get(email=self.mechanic['email'])
        jwt_token = get_jwt(self.user)
        self.auth_headers = {'HTTP_AUTHORIZATION': 'Bearer ' + jwt_token}

    def test_add_products(self):
        """
        creates a dummy product with add_product api with an image
        should get a valid response saying product created
        :return: None
        """
        product_details = {
            'name': 'test_Seat',
            'price': 10,
            'image_url': 'https://4.imimg.com/data4/NI/WE/MY-19393581/ciaz-car-seat-cover-500x500.jpg',
        }
        res = self.client.post('/api/shop/products', product_details, **self.auth_headers)
        self.assertEqual(res.status_code, 200)
