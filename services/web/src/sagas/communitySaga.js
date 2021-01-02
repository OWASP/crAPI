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
  NO_POSTS,
  NO_POST,
  POST_CREATED,
  POST_NOT_CREATED,
  COMMENT_ADDED,
  COMMENT_NOT_ADDED,
} from "../constants/messages";

/**
 * get the list of posts
 * @param { accessToken, callback} param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* getPosts(param) {
  const { accessToken, callback } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });

    const getUrl = APIService.GO_MICRO_SERVICES + requestURLS.GET_POSTS;
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
        type: actionTypes.FETCHED_POSTS,
        payload: ResponseJson,
      });
      callback(responseTypes.SUCCESS, ResponseJson);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, NO_POSTS);
  }
}

/**
 * get the post
 * @param { accessToken, callback, postId } param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* getPostById(param) {
  const { accessToken, callback, postId } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    
    const getUrl = APIService.GO_MICRO_SERVICES + requestURLS.GET_POST_BY_ID;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const ResponseJson = yield fetch(getUrl.replace("<postId>", postId), {
      headers,
      method: "GET",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.FETCHED_POST,
        payload: { postId, post: ResponseJson },
      });
      callback(responseTypes.SUCCESS, ResponseJson);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, NO_POST);
  }
}

/**
 * add new post
 * @param { accessToken, callback, post } param
 * accessToken: access token of the user
 * callback : callback method
 * post: post object to be added
 */
export function* addPost(param) {
  let recievedResponse = {};
  const { accessToken, callback, post } = param;
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    
    const postUrl = APIService.GO_MICRO_SERVICES + requestURLS.ADD_NEW_POST;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify(post),
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, POST_CREATED);
    } else {
      callback(responseTypes.FAILURE, POST_NOT_CREATED);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, POST_NOT_CREATED);
  }
}

/**
 * add new comment for the post
 * @param { accessToken, callback, postId, comment } param
 * accessToken: access token of the user
 * callback : callback method
 * post: post object to be added
 */
export function* addComment(param) {
  const { accessToken, callback, postId, comment } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });

    const postUrl = APIService.GO_MICRO_SERVICES + requestURLS.ADD_COMMENT;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const JsonResponse = yield fetch(postUrl.replace("<postId>", postId), {
      headers,
      method: "POST",
      body: JSON.stringify({ content: comment }),
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.FETCHED_POST,
        payload: { postId, post: JsonResponse },
      });
      callback(responseTypes.SUCCESS, COMMENT_ADDED);
    } else {
      callback(responseTypes.FAILURE, COMMENT_NOT_ADDED);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, COMMENT_NOT_ADDED);
  }
}

export function* communityActionWatcher() {
  yield takeLatest(actionTypes.GET_POSTS, getPosts);
  yield takeLatest(actionTypes.GET_POST_BY_ID, getPostById);
  yield takeLatest(actionTypes.ADD_POST, addPost);
  yield takeLatest(actionTypes.ADD_COMMENT, addComment);
}
