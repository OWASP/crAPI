/*
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

const initialData = {
  availableCredit: 0,
  products: [],
  pastOrders: [],
};

const profileReducer = (state = initialData, action) => {
  switch (action.type) {
    case actionTypes.BALANCE_CHANGED:
      return {
        ...state,
        availableCredit: action.payload.availableCredit,
      };
    case actionTypes.FETCHED_PRODUCTS:
      return {
        ...state,
        products: action.payload.products,
      };
    case actionTypes.FETCHED_ORDERS:
      return {
        ...state,
        pastOrders: action.payload.orders,
      };
    case actionTypes.FETCHED_ORDER:
      return {
        ...state,
        pastOrders: state.pastOrders.map((order) =>
          order.id === action.payload.orderId ? action.payload.order : order,
        ),
        order: action.payload.order,
      };
    case actionTypes.ORDER_RETURNED:
      return {
        ...state,
        pastOrders: state.pastOrders.map((order) =>
          order.id === action.payload.orderId ? action.payload.order : order,
        ),
      };
    default:
      return state;
  }
};
export default profileReducer;
