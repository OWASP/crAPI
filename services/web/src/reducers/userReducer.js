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
  fetchingData: false,
  isLoggedIn: false,
  accessToken: "",
  id: "",
  name: "",
  email: "",
  number: "",
  role: "",
};

const userReducer = (state = initialData, action) => {
  switch (action.type) {
    case actionTypes.FETCHING_DATA:
      return {
        ...state,
        fetchingData: true,
      };
    case actionTypes.FETCHED_DATA:
      return {
        ...state,
        fetchingData: false,
      };
    case actionTypes.LOGGED_IN:
      return {
        ...state,
        fetchingData: false,
        isLoggedIn: true,
        accessToken: action.payload.token,
        id: action.payload.user.id,
        name: action.payload.user.name,
        email: action.payload.user.email,
        number: action.payload.user.number,
        role: action.payload.user.role,
      };
    case actionTypes.FETCHED_USER:
      return {
        ...state,
        id: action.payload.id,
        name: action.payload.name,
        email: action.payload.email,
        number: action.payload.number,
        role: action.payload.role,
      };
    case actionTypes.LOG_OUT:
      console.log("Loddef out");
      return initialData;
    case actionTypes.INVALID_SESSION:
      return initialData;
    case actionTypes.PROFILE_PIC_CHANGED:
      return {
        ...state,
        userData: {
          ...state.userData,
          picture_url: action.payload.profilePicUrl,
        },
      };
    case actionTypes.VIDEO_CHANGED:
      return {
        ...state,
        userData: {
          ...state.userData,
          video_url: action.payload.videoUrl,
          video_id: action.payload.videoId,
        },
      };
    case actionTypes.VIDEO_NAME_CHANGED:
      return {
        ...state,
        userData: {
          ...state.userData,
          video_name: action.payload.videoName,
        },
      };
    case actionTypes.BALANCE_CHANGED:
      return {
        ...state,
        userData: {
          ...state.userData,
          available_credit: action.payload.availableCredit,
        },
      };
    default:
      return state;
  }
};
export default userReducer;
