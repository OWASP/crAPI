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

export const getProductsAction = ({ callback, accessToken }) => {
  return {
    type: actionTypes.GET_PRODUCTS,
    accessToken,
    callback,
  };
};

export const buyProductAction = ({ callback, accessToken, ...data }) => {
  return {
    type: actionTypes.BUY_PRODUCT,
    accessToken,
    callback,
    ...data,
  };
};

export const getOrdersAction = ({ callback, accessToken }) => {
  return {
    type: actionTypes.GET_ORDERS,
    accessToken,
    callback,
  };
};

export const returnOrderAction = ({ callback, accessToken, ...data }) => {
  return {
    type: actionTypes.RETURN_ORDER,
    accessToken,
    callback,
    ...data,
  };
};

export const applyCouponAction = ({ accessToken, callback, ...data }) => {
  return {
    type: actionTypes.APPLY_COUPON,
    accessToken,
    callback,
    ...data,
  };
};
