import actionTypes from "../constants/actionTypes";

export const uploadProfilePicAction = ({ callback, accessToken, ...data }) => {
  return {
    type: actionTypes.UPLOAD_PROFILE_PIC,
    accessToken,
    callback,
    ...data,
  };
};

export const uploadVideoAction = ({ accessToken, callback, ...data }) => {
  return {
    type: actionTypes.UPLOAD_VIDEO,
    accessToken,
    callback,
    ...data,
  };
};

export const changeVideoNameAction = ({ accessToken, callback, ...data }) => {
  return {
    type: actionTypes.CHANGE_VIDEO_NAME,
    accessToken,
    callback,
    ...data,
  };
};

export const convertVideoAction = ({ accessToken, callback, videoId }) => {
  return {
    type: actionTypes.CONVERT_VIDEO,
    accessToken,
    callback,
    videoId,
  };
};
