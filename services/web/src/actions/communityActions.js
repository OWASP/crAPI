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
