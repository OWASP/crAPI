/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import actionTypes from "../constants/actionTypes";

interface ActionPayload {
  accessToken: string;
  callback: () => void;
  [key: string]: any;
}

interface PostByIdPayload extends ActionPayload {
  postId: string;
}

interface AddCommentPayload extends PostByIdPayload {
  comment: string;
}

interface ReactToPostPayload extends PostByIdPayload {
  reaction: "like" | "dislike";
}

export const getPostsAction = ({
  accessToken,
  callback,
  ...data
}: ActionPayload) => {
  return {
    type: actionTypes.GET_POSTS,
    payload: { accessToken, ...data, callback },
  };
};

export const addPostAction = ({
  accessToken,
  callback,
  ...data
}: ActionPayload) => {
  return {
    type: actionTypes.ADD_POST,
    payload: { accessToken, ...data, callback },
  };
};

export const getPostByIdAction = ({
  accessToken,
  callback,
  postId,
}: PostByIdPayload) => {
  return {
    type: actionTypes.GET_POST_BY_ID,
    payload: { accessToken, postId, callback },
  };
};

export const addCommentAction = ({
  accessToken,
  callback,
  postId,
  comment,
}: AddCommentPayload) => {
  return {
    type: actionTypes.ADD_COMMENT,
    payload: { accessToken, postId, comment, callback },
  };
};

export const reactToPostAction = ({
  accessToken,
  callback,
  postId,
  reaction,
}: ReactToPostPayload) => {
  return {
    type: actionTypes.REACT_TO_POST,
    payload: { accessToken, postId, reaction, callback },
  };
};
