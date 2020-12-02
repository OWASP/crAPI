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
