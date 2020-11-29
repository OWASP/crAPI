import actionTypes from "../constants/actionTypes";

export const getPostsAction = ({ accessToken, callback }) => {
  return {
    type: actionTypes.GET_POSTS,
    accessToken,
    callback,
  };
};

export const addPostAction = ({ accessToken, callback, ...data }) => {
  return {
    type: actionTypes.ADD_POST,
    accessToken,
    callback,
    ...data,
  };
};

export const getPostByIdAction = ({ accessToken, callback, postId }) => {
  return {
    type: actionTypes.GET_POST_BY_ID,
    accessToken,
    callback,
    postId,
  };
};

export const addCommentAction = ({
  accessToken,
  callback,
  postId,
  comment,
}) => {
  return {
    type: actionTypes.ADD_COMMENT,
    accessToken,
    callback,
    postId,
    comment,
  };
};
