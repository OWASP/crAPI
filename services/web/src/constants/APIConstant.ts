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

import { crapienv } from "../config";

interface APIServiceType {
  IDENTITY_SERVICE: string;
  WORKSHOP_SERVICE: string;
  COMMUNITY_SERVICE: string;
  CHATBOT_SERVICE: string;
}

export const APIService: APIServiceType = {
  IDENTITY_SERVICE: crapienv.IDENTITY_SERVICE,
  WORKSHOP_SERVICE: crapienv.WORKSHOP_SERVICE,
  CHATBOT_SERVICE: crapienv.CHATBOT_SERVICE,
  COMMUNITY_SERVICE: crapienv.COMMUNITY_SERVICE,
};

interface RequestURLSType {
  [key: string]: string;
}

export const requestURLS: RequestURLSType = {
  LOGIN: "api/auth/login",
  UNLOCK: "api/auth/unlock",
  GET_USER: "api/v2/user/dashboard",
  SIGNUP: "api/auth/signup",
  RESET_PASSWORD: "api/v2/user/reset-password",
  FORGOT_PASSWORD: "api/auth/forget-password",
  VERIFY_OTP: "api/auth/v3/check-otp",
  LOGIN_TOKEN: "api/auth/v4.0/user/login-with-token",
  REGISTER_VEHICLE: "api/v2/vehicle/register_vehicle",
  ADD_VEHICLE: "api/v2/vehicle/add_vehicle",
  GET_VEHICLES: "api/v2/vehicle/vehicles",
  RESEND_MAIL: "api/v2/vehicle/resend_email",
  CHANGE_EMAIL: "api/v2/user/change-email",
  CHANGE_PHONE_NUMBER: "api/v2/user/change-phone-number",
  VERIFY_PHONE_NUMBER_OTP: "api/v2/user/verify-phone-otp",
  VERIFY_TOKEN: "api/v2/user/verify-email-token",
  UPLOAD_PROFILE_PIC: "api/v2/user/pictures",
  UPLOAD_VIDEO: "api/v2/user/videos",
  GET_VIDEO: "api/v2/user/videos/<videoId>",
  CHANGE_VIDEO_NAME: "api/v2/user/videos/<videoId>",
  REFRESH_LOCATION: "api/v2/vehicle/<carId>/location",
  CONVERT_VIDEO: "api/v2/user/videos/convert_video",
  CONTACT_MECHANIC: "api/merchant/contact_mechanic",
  RECEIVE_REPORT: "api/mechanic/receive_report",
  GET_MECHANICS: "api/mechanic",
  GET_PRODUCTS: "api/shop/products",
  GET_MECHANIC_SERVICES: "api/mechanic/service_requests",
  GET_MECHANIC_SERVICE: "api/mechanic/service_request/<serviceId>",
  CREATE_SERVICE_COMMENT: "api/mechanic/service_request/<serviceId>/comment",
  UPDATE_SERVICE_REQUEST_STATUS: "api/mechanic/service_request/<serviceId>",
  GET_VEHICLE_SERVICES: "api/merchant/service_requests/<vehicleVIN>",
  GET_SERVICE_REPORT: "api/mechanic/mechanic_report",
  DOWNLOAD_SERVICE_REPORT: "api/mechanic/download_report",
  BUY_PRODUCT: "api/shop/orders",
  GET_ORDERS: "api/shop/orders/all",
  GET_ORDER_BY_ID: "api/shop/orders/<orderId>",
  RETURN_ORDER: "api/shop/orders/return_order",
  APPLY_COUPON: "api/shop/apply_coupon",
  ADD_NEW_POST: "api/v2/community/posts",
  GET_POSTS: "api/v2/community/posts/recent",
  GET_POST_BY_ID: "api/v2/community/posts/<postId>",
  ADD_COMMENT: "api/v2/community/posts/<postId>/comment",
  LIKE_POST: "api/v2/community/posts/<postId>/like",
  DISLIKE_POST: "api/v2/community/posts/<postId>/dislike",
  VALIDATE_COUPON: "api/v2/coupon/validate-coupon",
  NEW_COUPON: "api/v2/coupon/new-coupon",
  VALIDATE_TOKEN: "api/auth/verify",
};
