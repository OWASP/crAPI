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

import actionTypes from "../constants/actionTypes";

export const logInUserAction = ({ email, password, callback }) => {
  return {
    type: actionTypes.LOG_IN,
    email,
    password,
    callback,
  };
};

export const signUpUserAction = ({
  name,
  email,
  number,
  password,
  callback,
}) => {
  return {
    type: actionTypes.SIGN_UP,
    name,
    email,
    number,
    password,
    callback,
  };
};

// clear store data and local storage and log user out
export const logOutUserAction = ({ callback }) => {
  return {
    type: actionTypes.LOG_OUT,
    callback,
  };
};

export const invalidSessionAction = () => {
  return {
    type: actionTypes.INVALID_SESSION,
  };
};

export const forgotPasswordAction = ({ callback, email }) => {
  return {
    type: actionTypes.FORGOT_PASSWORD,
    email,
    callback,
  };
};

export const verifyOTPAction = ({ callback, otp, email, password }) => {
  return {
    type: actionTypes.VERIFY_OTP,
    email,
    otp,
    callback,
    password,
  };
};

export const resetPasswordAction = ({
  callback,
  email,
  accessToken,
  password,
}) => {
  return {
    type: actionTypes.RESET_PASSWORD,
    email,
    accessToken,
    password,
    callback,
  };
};

export const getServicesAction = ({ callback, accessToken }) => {
  return {
    type: actionTypes.GET_SERVICES,
    accessToken,
    callback,
  };
};

export const changeEmailAction = ({ callback, accessToken, ...data }) => {
  return {
    type: actionTypes.CHANGE_EMAIL,
    accessToken,
    callback,
    ...data,
  };
};

export const verifyTokenAction = ({ callback, accessToken, ...data }) => {
  return {
    type: actionTypes.VERIFY_TOKEN,
    accessToken,
    callback,
    ...data,
  };
};
