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

export const verifyVehicleAction = ({ callback, accessToken, ...data }) => {
  return {
    type: actionTypes.VERIFY_VEHICLE,
    accessToken,
    callback,
    ...data,
  };
};

export const getMechanicsAction = ({ callback, accessToken }) => {
  return {
    type: actionTypes.GET_MECHANICS,
    accessToken,
    callback,
  };
};

export const getVehiclesAction = ({ callback, accessToken, email }) => {
  return {
    type: actionTypes.GET_VEHICLES,
    accessToken,
    callback,
    email,
  };
};

export const resendMailAction = ({ callback, accessToken }) => {
  return {
    type: actionTypes.RESEND_MAIL,
    accessToken,
    callback,
  };
};

export const contactMechanicAction = ({ callback, accessToken, ...data }) => {
  return {
    type: actionTypes.CONTACT_MECHANIC,
    accessToken,
    callback,
    ...data,
  };
};

export const refreshLocationAction = ({ accessToken, callback, ...data }) => {
  return {
    type: actionTypes.REFRESH_LOCATION,
    accessToken,
    callback,
    ...data,
  };
};
