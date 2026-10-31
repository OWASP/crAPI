/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React from "react";

export const PASSWORD_INFO: string =
  "Password must be of length 8-16 characters having one lower case, one upper case and one numeric character.";
export const NAME_REQUIRED: string = "Please enter your full name!";
export const EMAIL_REQUIRED: string = "Please enter a valid email!";
export const PHONE_NO_REQUIRED: string = "Please enter phone number";
export const INVALID_PHONE: string =
  "Contact number should only contain digits, (, ), + or spaces.";
export const PASSWORD_REQUIRED: string = "Please enter your password";
export const INVALID_PASSWORD: React.ReactElement = React.createElement(
  "span",
  { style: { color: "red" } },
  "Password should contain at least one digit, one small letter, one capital letter",
  React.createElement("br"),
  "letter and one special character.",
  React.createElement("br"),
  "Should be at least contain 8 characters and maximum 16 characters.",
  React.createElement("br"),
  "Allowed special characters are #$@!%&*?",
);
export const CONFIRM_PASSWORD: string = "Please confirm your password!";
export const PASSWORD_DO_NOT_MATCH: string = "Passwords do not match!";
export const OTP_REQUIRED: string = "Please enter the OTP!";
export const COMPANY_REQUIRED: string = "Please select a company!";
export const MODEL_REQUIRED: string = "Please select a brand!";
export const PIN_CODE_REQUIRED: string = "Please enter the pincode";
export const VEHICLE_NO_REQUIRED: string = "Please enter the vehicle number!";
export const INVALID_PIN_CODE: string = "Please enter a valid pin code";
export const VIN_REQUIRED: string = "Please enter a VIN of your vehivle";
export const MECHANIC_REQUIRED: string = "Please select a mechanic";
export const PROBLEM_REQUIRED: string =
  "Please describe problem of your vehicle";
export const TOKEN_REQUIRED: string =
  "Please enter the token sent to your new email id";
export const INVALID_VIN: string = "Please enter a valid VIN!";
export const VIDEO_NAME_REQUIRED: string = "Please enter video name!";
export const POST_TITLE_REQUIRED: string = "Please enter title for post!";
export const POST_DESC_REQUIRED: string = "Please enter description for Post!";
export const COMMENT_REQUIRED: string = "Please enter a comment!";
export const COUPON_CODE_REQUIRED: string = "Please enter a coupon code!";
export const PRODUCT_DETAILS_REQUIRED: string =
  "Please enter all product details!";
export const COUPON_AMOUNT_REQUIRED: string = "Please enter a coupon amount!";

export const NO_VEHICLE_DESC_1: string =
  "Your newly purchased Vehicle Details have been sent to you email address. Please check your email for the VIN and PIN code of your vehicle using the MailHog web portal.";
export const NO_VEHICLE_DESC_2: string = " Click here ";
export const NO_VEHICLE_DESC_3: string = "to send the information again";

export const SUCCESS_MESSAGE: string = "Success";
export const FAILURE_MESSAGE: string = "Failed";

export const NO_POSTS: string = "Could not get posts";
export const NO_POST: string = "Could not get post";
export const POST_CREATED: string = "Post Created.";
export const POST_NOT_CREATED: string = "Could not add new post";
export const COMMENT_ADDED: string = "Comment Added";
export const COMMENT_NOT_ADDED: string = "Could not add comment";
export const REACTION_NOT_SAVED: string = "Could not save your reaction";
export const PROFILE_PIC_UPDATED: string = "Profile pic updated successfully";
export const PROFILE_PIC_NOT_UPDATED: string = "Could not upload Picture";
export const VIDEO_UPDATED: string = "Video updated successfully";
export const VIDEO_NOT_UPDATED: string = "Could not upload video";
export const VIDEO_NAME_CHANGED: string = "Video Name Changed";
export const VIDEO_NAME_NOT_CHANGED: string = "Could not change video";
export const VIDEO_NOT_CONVERTED: string = "Could not convert video";
export const NO_PRODUCTS: string = "Could not get product details";
export const PRODUCT_NOT_BOUGHT: string = "Try again after sometime...";
export const NO_ORDERS: string = "Could not get orders";
export const NO_ORDER: string = "Could not get order";
export const ORDER_NOT_RETURNED: string = "Could not return order";
export const INVALID_COUPON_CODE: string = "Invalid Coupon Code";
export const COUPON_APPLIED: string = "Coupon applied";
export const COUPON_NOT_APPLIED: string = "Could not validate coupon";
export const COUPON_NOT_CREATED: string = "Could not create coupon";
export const PRODUCT_NOT_ADDED: string = "Could not add product";
export const NEW_PRODUCT_ADDED: string = "Product added!";
export const INVALID_CREDS: string = "Invalid Username or Password";
export const INVALID_CODE_CREDS: string = "Invalid Email or Code";
export const SIGN_UP_SUCCESS: string = "User Registered Successfully!";
export const SIGN_UP_FAILED: string = "Could not sign up";
export const OTP_SENT: string = "OTP has been sent to your registered emailid";
export const OTP_NOT_SENT: string = "Could not send OTP";
export const OTP_VERIFIED: string =
  "Otp verified and your password has been set.";
export const OTP_NOT_VERIFIED: string = "Could not verify OTP";
export const PASSWORD_CHANGED: string = "Password Reset Successfully";
export const PASSWORD_NOT_CHANGED: string = "Could not reset your password";
export const TOKEN_NOT_SENT: string = "Could not send token to email address";
export const EMAIL_CHANGED: string = "Token Verified and email changed";
export const EMAIL_NOT_CHANGED: string = "Could not change email id";
export const NO_SERVICES: string = "Could not get mechanic details";
export const EMAIL_NOT_SENT: string = "Could not resend mail";
export const VEHICLE_NOT_ADDED: string = "Internal Server error! Wrong VIN!";
export const VEHICLE_NOT_REGISTERED: string =
  "Could not register vehicle. Try again!";
export const NO_VEHICLES: string = "Could not get vehicles";
export const NO_MECHANICS: string = "Could not get mechanic details";
export const SERVICE_REQUEST_SENT: string =
  "Service Request sent to the mechanic";
export const SERVICE_REQUEST_NOT_SENT: string = "Could not contact mechanic";
export const LOC_NOT_REFRESHED: string = "Could not refresh location";
