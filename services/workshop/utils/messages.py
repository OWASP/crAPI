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
contains all constant messages
which can be used in views
"""
METHOD_NOT_ALLOWED = "Method Not Allowed!"
BAD_REQUEST = "Bad Request!"
JWT_REQUIRED = "JWT Token required!"
INVALID_TOKEN = "Invalid JWT Token!"
TOKEN_EXPIRED = "Token Expired!"
EMAIL_ALREADY_EXISTS = "Email already Registered!"
MEC_CODE_ALREADY_EXISTS = "Mechanic Code already exists!"
MEC_CREATED = "Mechanic created with email: {}"
NO_OF_REPEATS_EXCEEDED = "Service unavailable. Seems like you caused layer 7 DoS :)"
MIN_NO_OF_REPEATS_FAILED = " 'number_of_repeats' should be between 1 and 100."
ERROR_UPLOADING_FILE = "Error Uploading File!"
PRODUCT_SAVED = "Product saved with id {}"
INSUFFICIENT_BALANCE = "Insufficient Balance. Please apply coupons to get more balance!"
ORDER_CREATED = "Order sent successfully."
ORDER_RETURNED_PENDING = "This order is already requested for returning!"
ORDER_ALREADY_RETURNED = "This order is already returned!"
ORDER_RETURNING = "Please use the following QR code to return your order to a UPS store!"
COUPON_ALREADY_APPLIED = "This coupon code is already claimed by you!! Please try with another coupon code"
COUPON_APPLIED = "Coupon successfully applied!"
RESTRICTED = "You are not allowed to access this resource!"
INVALID_STATUS = "The value of 'status' has to be 'delivered','return pending' or 'returned'"
REPORT_ID_MISSING = "Please enter the report_id value."
INVALID_REPORT_ID = "Please enter a valid report_id value."
REPORT_DOES_NOT_EXIST = "The Report does not exist for given report_id."
COULD_NOT_CONNECT = "Could not connect to mechanic api."
