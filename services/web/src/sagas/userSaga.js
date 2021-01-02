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
  INVALID_CREDS, SIGN_UP_SUCCESS, SIGN_UP_FAILED, OTP_SENT, OTP_NOT_SENT, OTP_VERIFIED, OTP_NOT_VERIFIED, PASSWORD_CHANGED, PASSWORD_NOT_CHANGED, TOKEN_NOT_SENT, EMAIL_CHANGED, EMAIL_NOT_CHANGED, NO_SERVICES,
} from "../constants/messages";

/**
 * request for login
 * @param {email, password, callback} param
 * email: user email,
 * password: user password,
 * callback : callback method
 */
export function* logIn(param) {
  const { email, password, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });

    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.LOGIN;
    let headers = {
      "Content-Type": "application/json",
    };

    const responseJSON = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ email, password }),
    }).then((response) => {
      recievedResponse = response;
      if (recievedResponse.ok) return response.json();
      return response;
    });

    if (!recievedResponse.ok) {
      throw responseJSON;
    }

    const getUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.GET_USER;
    headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${responseJSON.token}`,
    };

    const userResponseJSON = yield fetch(getUrl, {
      headers,
      method: "GET",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    if (!recievedResponse.ok) {
      throw responseJSON;
    }

    yield put({
      type: actionTypes.LOGGED_IN,
      payload: { token: responseJSON.token, user: userResponseJSON },
    });
    callback(responseTypes.SUCCESS, responseJSON.data);
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, INVALID_CREDS);
  }
}

/**
 * request for new signup
 * @param {email, password, callback} param
 * name: user name,
 * email: user email,
 * number: user number,
 * password: user password,
 * callback : callback method
 */
export function* signUp(param) {
  const { name, email, number, password, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });

    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.SIGNUP;
    const headers = {
      "Content-Type": "application/json",
    };
    const responseJSON = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ name, email, number, password }),
    }).then((response) => {
      recievedResponse = response;
      if (recievedResponse.ok) return response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) callback(responseTypes.SUCCESS, SIGN_UP_SUCCESS);
    else callback(responseTypes.FAILURE, responseJSON.message);
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, SIGN_UP_FAILED);
  }
}

/**
 * send OTP for forgot password
 * @param {data, callback} param
 * email: user email,
 * callback : callback method
 */
export function* forgotPassword(param) {
  const { callback, email } = param;

  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl =
      APIService.JAVA_MICRO_SERVICES + requestURLS.FORGOT_PASSWORD;
    const headers = {
      "Content-Type": "application/json",
    };

    const responseJSON = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ email }),
    }).then((response) => {
      recievedResponse = response;
      if (recievedResponse.ok) return response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) callback(responseTypes.SUCCESS, OTP_SENT);
    else callback(responseTypes.FAILURE, responseJSON.message);
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, OTP_NOT_SENT);
  }
}

/**
 * verify OTP for forgot password
 * @param {data, callback} param
 * email: user email,
 * otp: otp recieved through mail,
 * password: new password
 * callback : callback method
 */
export function* verifyOTP(param) {
  const { callback, email, otp, password } = param;

  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.VERIFY_OTP;
    const headers = {
      "Content-Type": "application/json",
    };

    const responseJSON = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ email, otp, password }),
    }).then((response) => {
      recievedResponse = response;
      if (recievedResponse.ok) return response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) callback(responseTypes.SUCCESS, OTP_VERIFIED);
    else if (recievedResponse.status === 503)
      callback(responseTypes.REDIRECT, responseJSON.message);
    else callback(responseTypes.FAILURE, responseJSON.message);
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, OTP_NOT_VERIFIED);
  }
}

/**
 * change user password
 * @param {password, accessToken callback} param
 * accessToken: access token of the user
 * password: new password
 * callback : callback method
 */
export function* resetPassword(param) {
  const { accessToken, password, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.RESET_PASSWORD;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };

    yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ password }),
    }).then((response) => {
      recievedResponse = response;
      return response;
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, PASSWORD_CHANGED);
    } else {
      callback(responseTypes.FAILURE, PASSWORD_NOT_CHANGED);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, PASSWORD_NOT_CHANGED);
  }
}

/**
 * request for changing email
 * @param {new_email, old_email, accessToken callback} param
 * accessToken: access token of the user
 * new_email: new email id entered by the user
 * old_email: old email id of the user
 * callback : callback method
 */
export function* changeEmail(param) {
  const { accessToken, callback, old_email, new_email } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.CHANGE_EMAIL;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };

    const responseJSON = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ old_email, new_email }),
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, responseJSON.message);
    } else {
      callback(responseTypes.FAILURE, responseJSON.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, TOKEN_NOT_SENT);
  }
}

/**
 * verify token and change email
 * @param {new_email, token, accessToken callback} param
 * accessToken: access token of the user
 * new_email: new email id entered by the user
 * token: token sent to new_email id
 * callback : callback method
 */
export function* verifyToken(param) {
  const { accessToken, callback, new_email, token } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.VERIFY_TOKEN;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };

    const responseJSON = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ new_email, token }),
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, EMAIL_CHANGED);
    } else {
      callback(responseTypes.FAILURE, responseJSON.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, EMAIL_NOT_CHANGED);
  }
}

/**
 * get the list of services alloted to this mechanic
 * @param { accessToken, callback} param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* getServices(param) {
  const { accessToken, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const getUrl =
      APIService.PYTHON_MICRO_SERVICES + requestURLS.GET_SERVICES;
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
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, ResponseJson.service_requests);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, NO_SERVICES);
  }
}

export function* userActionWatcher() {
  yield takeLatest(actionTypes.LOG_IN, logIn);
  yield takeLatest(actionTypes.SIGN_UP, signUp);
  yield takeLatest(actionTypes.VERIFY_OTP, verifyOTP);
  yield takeLatest(actionTypes.FORGOT_PASSWORD, forgotPassword);
  yield takeLatest(actionTypes.RESET_PASSWORD, resetPassword);
  yield takeLatest(actionTypes.CHANGE_EMAIL, changeEmail);
  yield takeLatest(actionTypes.VERIFY_TOKEN, verifyToken);
  yield takeLatest(actionTypes.GET_SERVICES, getServices);
}
