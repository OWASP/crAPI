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
  NO_PRODUCTS,
  PRODUCT_NOT_BOUGHT,
  NO_ORDERS,
  ORDER_NOT_RETURNED,
  INVALID_COUPON_CODE,
  COUPON_APPLIED,
  COUPON_NOT_APPLIED,
} from "../constants/messages";

/**
 * get the list of products
 * @param { accessToken, callback} param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* getProducts(param) {
  const { accessToken, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const getUrl =
      APIService.PYTHON_MICRO_SERVICES + requestURLS.GET_PRODUCTS;
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
      yield put({
        type: actionTypes.BALANCE_CHANGED,
        payload: { availableCredit: ResponseJson.credit },
      });
      yield put({
        type: actionTypes.FETCHED_PRODUCTS,
        payload: { products: ResponseJson.products },
      });
      callback(responseTypes.SUCCESS, ResponseJson);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, NO_PRODUCTS);
  }
}

/**
 * buy a product
 * @param { accessToken, callback, product_id} param
 * accessToken: access token of the user
 * callback : callback method
 * product_id: id of the product which is to be bought
 */
export function* buyProduct(param) {
  const { accessToken, callback, productId } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl =
      APIService.PYTHON_MICRO_SERVICES + requestURLS.BUY_PRODUCT;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const ResponseJson = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ product_id: productId, quantity: 1 }),
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.BALANCE_CHANGED,
        payload: { availableCredit: ResponseJson.credit },
      });
      callback(responseTypes.SUCCESS, ResponseJson.message);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, PRODUCT_NOT_BOUGHT);
  }
}

/**
 * get the list of orders ordered by this user
 * @param { accessToken, callback} param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* getOrders(param) {
  const { accessToken, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const getUrl = APIService.PYTHON_MICRO_SERVICES + requestURLS.GET_ORDERS;
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
      yield put({
        type: actionTypes.FETCHED_ORDERS,
        payload: { orders: ResponseJson.orders },
      });
      callback(responseTypes.SUCCESS, ResponseJson);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, NO_ORDERS);
  }
}

/**
 * return an order
 * @param { accessToken, callback, orderId } param
 * accessToken: access token of the user
 * callback : callback method
 * orderId: id of the order to be returned
 */
export function* returnOrder(param) {
  const { accessToken, callback, orderId } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl =
      APIService.PYTHON_MICRO_SERVICES + requestURLS.RETURN_ORDER;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const ResponseJson = yield fetch(`${postUrl}?order_id=${orderId}`, {
      headers,
      method: "POST",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.ORDER_RETURNED,
        payload: { order: ResponseJson.order, orderId },
      });
      callback(responseTypes.SUCCESS, ResponseJson);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, ORDER_NOT_RETURNED);
  }
}

/**
 * validate the coupon and increase user credit
 * @param { accessToken, callback, couponCode} param
 * accessToken: access token of the user
 * callback : callback method
 * couponCode: coupon code of the coupon
 */
export function* applyCoupon(param) {
  const { accessToken, callback, couponCode } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    let postUrl = APIService.GO_MICRO_SERVICES + requestURLS.VALIDATE_COUPON;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const CouponJson = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify({ coupon_code: couponCode }),
    }).then((response) => {
      recievedResponse = response;
      if (recievedResponse.ok) return response.json();
      return response;
    });

    if (!recievedResponse.ok) {
      yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
      callback(responseTypes.FAILURE, INVALID_COUPON_CODE);
    } else {
      postUrl = APIService.PYTHON_MICRO_SERVICES + requestURLS.APPLY_COUPON;
      const ResponseJson = yield fetch(postUrl, {
        headers,
        method: "POST",
        body: JSON.stringify({
          coupon_code: CouponJson.coupon_code,
          amount: parseFloat(CouponJson.amount),
        }),
      }).then((response) => {
        recievedResponse = response;
        return response.json();
      });

      yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
      if (recievedResponse.ok) {
        yield put({
          type: actionTypes.BALANCE_CHANGED,
          payload: { availableCredit: ResponseJson.credit },
        });
        callback(responseTypes.SUCCESS, COUPON_APPLIED);
      } else {
        callback(responseTypes.FAILURE, ResponseJson.message);
      }
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, COUPON_NOT_APPLIED);
  }
}

export function* shopActionWatcher() {
  yield takeLatest(actionTypes.GET_PRODUCTS, getProducts);
  yield takeLatest(actionTypes.BUY_PRODUCT, buyProduct);
  yield takeLatest(actionTypes.GET_ORDERS, getOrders);
  yield takeLatest(actionTypes.RETURN_ORDER, returnOrder);
  yield takeLatest(actionTypes.APPLY_COUPON, applyCoupon);
}
