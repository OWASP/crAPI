/*
 * Copyright 2020 Traceable, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { crapienv } from "../config.js"

export const APIService = {
  JAVA_MICRO_SERVICES: crapienv.JAVA_MICRO_SERVICES,
  PYTHON_MICRO_SERVICES: crapienv.PYTHON_MICRO_SERVICES,
  GO_MICRO_SERVICES: crapienv.GO_MICRO_SERVICES,
};

export const requestURLS = {

  LOGIN: "api/auth/login",
  GET_USER: "api/v2/user/dashboard",
  SIGNUP: "api/auth/signup",
  RESET_PASSWORD: "api/v2/user/reset-password",
  // SEND_MAGIC_URL: "api/auth/magic-signup",
  // VERIFY_MAGIC_URL: "api/auth/magic-url",
  FORGOT_PASSWORD: "api/auth/forget-password",
  VERIFY_OTP: "api/auth/v3/check-otp",
  LOGIN_TOKEN: "api/auth/v4.0/user/login-with-token",
  // GET_COMPANIES: "api/v2/user/get-company",
  // GET_MODELS: "api/v2/user/get-model",
  ADD_VEHICLE: "api/v2/vehicle/add_vehicle",
  GET_VEHICLES: "api/v2/vehicle/vehicles",
  RESEND_MAIL: "api/v2/vehicle/resend_email",
  CHANGE_EMAIL: "api/v2/user/change-email",
  VERIFY_TOKEN: "api/v2/user/verify-email-token",
  UPLOAD_PROFILE_PIC: "api/v2/user/pictures",
  UPLOAD_VIDEO: "api/v2/user/videos",
  CHANGE_VIDEO_NAME: "api/v2/user/videos/<videoId>",
  REFRESH_LOCATION: "api/v2/vehicle/<carId>/location",
  CONVERT_VIDEO: "api/v2/user/videos/convert_video",
  CONTACT_MECHANIC: "api/merchant/contact_mechanic",
  RECEIVE_REPORT: "api/mechanic/receive_report",
  GET_MECHANICS: "api/mechanic",
  GET_PRODUCTS: "api/shop/products",
  GET_SERVICES: "api/mechanic/service_requests",
  BUY_PRODUCT: "api/shop/orders",
  GET_ORDERS: "api/shop/orders/all",
  RETURN_ORDER: "api/shop/orders/return_order",
  APPLY_COUPON: "api/shop/apply_coupon",
  ADD_NEW_POST: "api/v2/community/posts",
  GET_POSTS: "api/v2/community/posts/recent",
  GET_POST_BY_ID: "api/v2/community/posts/<postId>",
  ADD_COMMENT: "api/v2/community/posts/<postId>/comment",
  VALIDATE_COUPON: "api/v2/coupon/validate-coupon",
};
