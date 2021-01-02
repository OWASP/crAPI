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

import { put, takeLatest } from "redux-saga/effects";
import { APIService, requestURLS } from "../constants/APIConstant";
import actionTypes from "../constants/actionTypes";
import responseTypes from "../constants/responseTypes";
import {
  EMAIL_NOT_SENT,
  VEHICLE_NOT_ADDED,
  NO_VEHICLES,
  NO_MECHANICS,
  SERVICE_REQUEST_SENT,
  SERVICE_REQUEST_NOT_SENT,
  LOC_NOT_REFRESHED,
} from "../constants/messages";

/**
 * resend vehicle details
 * @param { accessToken, callback } param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* resendMail(param) {
  const { accessToken, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });

    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.RESEND_MAIL;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const ResponseJson = yield fetch(postUrl, {
      headers,
      method: "POST",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, ResponseJson.message);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, EMAIL_NOT_SENT);
  }
}

/**
 * verify vehicle details entered by user and add this vehicle to this uder
 * @param { pincode, vehicleNumber, accessToken, callback} param
 * pincode: pincode of the vehicle entered
 * vehicleNumber: vehicle number entered by the user
 * accessToken: access token of the user
 * callback : callback method
 */
export function* verifyVehicle(param) {
  const { accessToken, callback, pinCode, vin } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.ADD_VEHICLE;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const ResponseJson = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ vin, pincode: pinCode }),
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, ResponseJson.message);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, VEHICLE_NOT_ADDED);
  }
}

/**
 * get the list of vehicles of the current user
 * @param { accessToken, callback} param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* getVehicles(param) {
  const { accessToken, callback } = param;
  console.log(callback);
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    let getUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.GET_USER;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const userResponseJSON = yield fetch(getUrl, {
      headers,
      method: "GET",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });
    if (!recievedResponse.ok) {
      throw userResponseJSON;
    }
    yield put({
      type: actionTypes.FETCHED_USER,
      payload: userResponseJSON,
    });

    getUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.GET_VEHICLES;
    const ResponseJson = yield fetch(getUrl, {
      headers,
      method: "GET",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.FETCHED_VEHICLES,
        payload: ResponseJson,
      });
      callback(responseTypes.SUCCESS, ResponseJson);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.error);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, NO_VEHICLES);
  }
}

/**
 * get the list of mechanics
 * @param { accessToken, callback} param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* getMechanics(param) {
  const { accessToken, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const getUrl =
      APIService.PYTHON_MICRO_SERVICES + requestURLS.GET_MECHANICS;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const ResponseJson = yield fetch(getUrl, {
      headers,
      method: "GET",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    yield put({
      type: actionTypes.FETCHED_MECHANICS,
      payload: ResponseJson.mechanics,
    });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, ResponseJson.mechanics);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, NO_MECHANICS);
  }
}

/**
 * contact Mechanic API
 * @param { accessToken, callback, VIN, mechanic, problem_details } param
 * accessToken: access token of the user
 * callback : callback method
 * VIN: vehicle identification number
 * mechanic: mechanic_code to whom service is to be given
 * problem_details: Problem about the car
 */
export function* contactMechanic(param) {
  const { accessToken, callback, mechanicCode, problemDetails, vin } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl =
      APIService.PYTHON_MICRO_SERVICES + requestURLS.CONTACT_MECHANIC;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    var http_host = (new URL(window.location.href)).origin;
    const ResponseJson = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({
        mechanic_code: mechanicCode,
        problem_details: problemDetails,
        vin,
        mechanic_api:
	      http_host+"/"+APIService.PYTHON_MICRO_SERVICES + requestURLS.RECEIVE_REPORT,
        repeat_request_if_failed: false,
        number_of_repeats: 1,
      }),
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, SERVICE_REQUEST_SENT);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, SERVICE_REQUEST_NOT_SENT);
  }
}

/**
 * change location of the vehicle
 * @param { accessToken, callback, vehicle_id } param
 * accessToken: access token of the user
 * callback : callback method
 * vehicle_id: vehicle_id of the vehicle whose location is to be changed
 */
export function* refreshLocation(param) {
  const { accessToken, callback, carId } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const getUrl =
      APIService.JAVA_MICRO_SERVICES + requestURLS.REFRESH_LOCATION;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const ResponseJson = yield fetch(getUrl.replace("<carId>", carId), {
      headers,
      method: "GET",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.REFRESHED_LOCATION,
        payload: { carId, location: ResponseJson.vehicleLocation },
      });
      callback(responseTypes.SUCCESS, ResponseJson.vehicleLocation);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, LOC_NOT_REFRESHED);
  }
}

export function* vehicleActionWatcher() {
  yield takeLatest(actionTypes.RESEND_MAIL, resendMail);
  yield takeLatest(actionTypes.VERIFY_VEHICLE, verifyVehicle);
  yield takeLatest(actionTypes.GET_VEHICLES, getVehicles);
  yield takeLatest(actionTypes.GET_MECHANICS, getMechanics);
  yield takeLatest(actionTypes.CONTACT_MECHANIC, contactMechanic);
  yield takeLatest(actionTypes.REFRESH_LOCATION, refreshLocation);
}
