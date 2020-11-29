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
