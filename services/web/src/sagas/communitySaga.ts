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

import { put, takeEvery, takeLatest } from "redux-saga/effects";
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
  REACTION_NOT_SAVED,
} from "../constants/messages";
import MyAction from "../types/action";

interface ReceivedResponse extends Response {
  ok: boolean;
}

/**
 * get the list of posts
 * @payload { accessToken, callback} payload
 * accessToken: access token of the user
 * callback : callback method
 * offset : offset for the posts
 */
export function* getPosts(action: MyAction): Generator<any, void, any> {
  console.log("getPosts", action);
  const { accessToken, callback, offset = 0 } = action.payload;
  let receivedResponse: ReceivedResponse = {} as ReceivedResponse;
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const getUrl =
      APIService.COMMUNITY_SERVICE +
      requestURLS.GET_POSTS +
      `?limit=30&offset=${offset}`;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const responseJson = yield fetch(getUrl, {
      headers,
      method: "GET",
    }).then((response: Response) => {
      receivedResponse = response as ReceivedResponse;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: receivedResponse });
    if (receivedResponse.ok) {
      yield put({
        type: actionTypes.FETCHED_POSTS,
        payload: responseJson,
      });
      callback(responseTypes.SUCCESS, responseJson);
    } else {
      callback(responseTypes.FAILURE, responseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: receivedResponse });
    callback(responseTypes.FAILURE, NO_POSTS);
  }
}

/**
 * get the post
 * @payload { accessToken, callback, postId } payload
 * accessToken: access token of the user
 * callback : callback method
 */
export function* getPostById(action: MyAction): Generator<any, void, any> {
  const { accessToken, callback, postId } = action.payload;
  let receivedResponse: ReceivedResponse = {} as ReceivedResponse;
  try {
    yield put({ type: actionTypes.FETCHING_DATA });

    const getUrl = APIService.COMMUNITY_SERVICE + requestURLS.GET_POST_BY_ID;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const responseJson = yield fetch(getUrl.replace("<postId>", postId), {
      headers,
      method: "GET",
    }).then((response: Response) => {
      receivedResponse = response as ReceivedResponse;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: receivedResponse });
    if (receivedResponse.ok) {
      yield put({
        type: actionTypes.FETCHED_POST,
        payload: { postId, post: responseJson },
      });
      callback(responseTypes.SUCCESS, responseJson);
    } else {
      callback(responseTypes.FAILURE, responseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: receivedResponse });
    callback(responseTypes.FAILURE, NO_POST);
  }
}

/**
 * add new post
 * @payload { accessToken, callback, post } payload
 * accessToken: access token of the user
 * callback : callback method
 * post: post object to be added
 */
export function* addPost(action: MyAction): Generator<any, void, any> {
  let receivedResponse: ReceivedResponse = {} as ReceivedResponse;
  const { accessToken, callback, post } = action.payload;
  try {
    yield put({ type: actionTypes.FETCHING_DATA });

    const postUrl = APIService.COMMUNITY_SERVICE + requestURLS.ADD_NEW_POST;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    yield fetch(postUrl, {
      headers,
      method: "POST",
      body: JSON.stringify(post),
    }).then((response: Response) => {
      receivedResponse = response as ReceivedResponse;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: receivedResponse });
    if (receivedResponse.ok) {
      callback(responseTypes.SUCCESS, POST_CREATED);
    } else {
      callback(responseTypes.FAILURE, POST_NOT_CREATED);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: receivedResponse });
    callback(responseTypes.FAILURE, POST_NOT_CREATED);
  }
}

/**
 * add new comment for the post
 * @payload { accessToken, callback, postId, comment } payload
 * accessToken: access token of the user
 * callback : callback method
 * post: post object to be added
 */
export function* addComment(action: MyAction): Generator<any, void, any> {
  const { accessToken, callback, postId, comment } = action.payload;
  let receivedResponse: ReceivedResponse = {} as ReceivedResponse;
  try {
    yield put({ type: actionTypes.FETCHING_DATA });

    const postUrl = APIService.COMMUNITY_SERVICE + requestURLS.ADD_COMMENT;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const JsonResponse = yield fetch(postUrl.replace("<postId>", postId), {
      headers,
      method: "POST",
      body: JSON.stringify({ content: comment }),
    }).then((response: Response) => {
      receivedResponse = response as ReceivedResponse;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: receivedResponse });
    if (receivedResponse.ok) {
      yield put({
        type: actionTypes.FETCHED_POST,
        payload: { postId, post: JsonResponse },
      });
      callback(responseTypes.SUCCESS, COMMENT_ADDED);
    } else {
      callback(responseTypes.FAILURE, COMMENT_NOT_ADDED);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: receivedResponse });
    callback(responseTypes.FAILURE, COMMENT_NOT_ADDED);
  }
}

/**
 * like or dislike a post (toggles: reacting the same way again removes it)
 * @payload { accessToken, callback, postId, reaction } payload
 * accessToken: access token of the user
 * callback : callback method
 * reaction: "like" or "dislike"
 */
export function* reactToPost(action: MyAction): Generator<any, void, any> {
  const { accessToken, callback, postId, reaction } = action.payload;
  let receivedResponse: ReceivedResponse = {} as ReceivedResponse;
  try {
    const path =
      reaction === "like" ? requestURLS.LIKE_POST : requestURLS.DISLIKE_POST;
    const postUrl = APIService.COMMUNITY_SERVICE + path;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const JsonResponse = yield fetch(postUrl.replace("<postId>", postId), {
      headers,
      method: "POST",
    }).then((response: Response) => {
      receivedResponse = response as ReceivedResponse;
      return response.json();
    });

    if (receivedResponse.ok) {
      yield put({
        type: actionTypes.FETCHED_POST,
        payload: { postId, post: JsonResponse },
      });
      callback(responseTypes.SUCCESS, JsonResponse);
    } else {
      callback(responseTypes.FAILURE, REACTION_NOT_SAVED);
    }
  } catch (e) {
    callback(responseTypes.FAILURE, REACTION_NOT_SAVED);
  }
}

export function* communityActionWatcher(): Generator<any, void, any> {
  yield takeLatest(actionTypes.GET_POSTS, getPosts);
  yield takeLatest(actionTypes.GET_POST_BY_ID, getPostById);
  yield takeLatest(actionTypes.ADD_POST, addPost);
  yield takeLatest(actionTypes.ADD_COMMENT, addComment);
  yield takeEvery(actionTypes.REACT_TO_POST, reactToPost);
}
