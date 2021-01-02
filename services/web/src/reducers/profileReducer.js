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

const initialData = {
  videoId: "",
  videoData: "",
  videoName: "",
  profilePicData: "",
};

const profileReducer = (state = initialData, action) => {
  switch (action.type) {
    case actionTypes.LOGGED_IN:
      return {
        ...state,
        videoId: action.payload.video_id,
        videoData: action.payload.video_url,
        videoName: action.payload.video_name,
        profilePicData: action.payload.picture_url,
      };
    case actionTypes.FETCHED_USER:
      return {
        ...state,
        videoId: action.payload.video_id,
        videoData: action.payload.video_url,
        videoName: action.payload.video_name,
        profilePicData: action.payload.picture_url,
      };
    case actionTypes.PROFILE_PIC_CHANGED:
      return {
        ...state,
        profilePicData: action.payload.profilePicData,
      };
    case actionTypes.VIDEO_CHANGED:
      return {
        ...state,
        videoId: action.payload.videoId,
        videoData: action.payload.videoData,
      };
    case actionTypes.VIDEO_NAME_CHANGED:
      return {
        ...state,
        videoName: action.payload.videoName,
      };
    default:
      return state;
  }
};
export default profileReducer;
