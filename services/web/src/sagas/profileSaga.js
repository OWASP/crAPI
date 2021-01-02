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
  PROFILE_PIC_UPDATED,
  PROFILE_PIC_NOT_UPDATED,
  VIDEO_UPDATED,
  VIDEO_NOT_UPDATED,
  VIDEO_NAME_CHANGED,
  VIDEO_NAME_NOT_CHANGED,
  VIDEO_NOT_CONVERTED,
} from "../constants/messages";

/**
 * Upload profile pic
 * @param { accessToken, callback, file } param
 * accessToken: access token of the user
 * callback : callback method
 * file: image file
 */
export function* uploadProfilePic(param) {
  const { accessToken, callback, file } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl =
      APIService.JAVA_MICRO_SERVICES + requestURLS.UPLOAD_PROFILE_PIC;
    const headers = {
      // "Content-Type": "multipart/form-data;",
      Authorization: `Bearer ${accessToken}`,
    };
    const formData = new FormData();
    formData.append("file", file);
    const ResponseJson = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: formData,
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.PROFILE_PIC_CHANGED,
        payload: { profilePicData: ResponseJson.picture },
      });
      callback(responseTypes.SUCCESS, PROFILE_PIC_UPDATED);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, PROFILE_PIC_NOT_UPDATED);
  }
}

/**
 * Upload car video
 * @param { accessToken, callback, file } param
 * accessToken: access token of the user
 * callback : callback method
 * file: video file
 */
export function* uploadVideo(param) {
  const { accessToken, callback, file } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const postUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.UPLOAD_VIDEO;
    const headers = {
      // "Content-Type": "multipart/form-data;",
      Authorization: `Bearer ${accessToken}`,
    };
    const formData = new FormData();
    formData.append("file", file);
    const ResponseJson = yield fetch(postUrl, {
      headers,
      method: "POST",
      body: formData,
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.VIDEO_CHANGED,
        payload: {
          videoData: ResponseJson.profileVideo,
          videoId: ResponseJson.id,
        },
      });
      callback(responseTypes.SUCCESS, VIDEO_UPDATED);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, VIDEO_NOT_UPDATED);
  }
}

/**
 * change Video Name
 * @param { accessToken, callback, videoName } param
 * accessToken: access token of the user
 * callback : callback method
 * videoName : new video name
 */
export function* changeVideoName(param) {
  const { accessToken, callback, videoName, videoId } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const putUrl =
      APIService.JAVA_MICRO_SERVICES +
      requestURLS.CHANGE_VIDEO_NAME.replace("<videoId>", videoId);
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    const ResponseJson = yield fetch(putUrl, {
      headers,
      method: "PUT",
      body: JSON.stringify({ videoName }),
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      yield put({
        type: actionTypes.VIDEO_NAME_CHANGED,
        payload: { videoName: ResponseJson.video_name },
      });
      callback(responseTypes.SUCCESS, VIDEO_NAME_CHANGED);
    } else {
      callback(responseTypes.FAILURE, ResponseJson.message);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, VIDEO_NAME_NOT_CHANGED);
  }
}

/**
 * convert video
 * @param { accessToken, callback } param
 * accessToken: access token of the user
 * callback : callback method
 */
export function* convertVideo(param) {
  const { accessToken, callback, videoId } = param;
  let recievedResponse = {};
  try {
    yield put({ type: actionTypes.FETCHING_DATA });
    const getUrl = APIService.JAVA_MICRO_SERVICES + requestURLS.CONVERT_VIDEO;
    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };
    yield fetch(`${getUrl}?video_id=${videoId}`, {
      headers,
      method: "GET",
    }).then((response) => {
      recievedResponse = response;
      return response.json();
    });

    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    if (recievedResponse.ok) {
      callback(responseTypes.SUCCESS, VIDEO_NOT_CONVERTED);
    } else {
      callback(responseTypes.FAILURE, VIDEO_NOT_CONVERTED);
    }
  } catch (e) {
    yield put({ type: actionTypes.FETCHED_DATA, payload: recievedResponse });
    callback(responseTypes.FAILURE, VIDEO_NOT_CONVERTED);
  }
}

export function* profileActionWatcher() {
  yield takeLatest(actionTypes.UPLOAD_PROFILE_PIC, uploadProfilePic);
  yield takeLatest(actionTypes.UPLOAD_VIDEO, uploadVideo);
  yield takeLatest(actionTypes.CHANGE_VIDEO_NAME, changeVideoName);
  yield takeLatest(actionTypes.CONVERT_VIDEO, convertVideo);
}
