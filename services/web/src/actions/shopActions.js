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
