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
  vehicles: [],
  mechanics: [],
};

const vehicleReducer = (state = initialData, action) => {
  switch (action.type) {
    case actionTypes.FETCHED_VEHICLES:
      return {
        ...state,
        vehicles: action.payload,
      };
    case actionTypes.FETCHED_MECHANICS:
      return {
        ...state,
        mechanics: action.payload,
      };
    case actionTypes.REFRESHED_LOCATION:
      return {
        ...state,
        vehicles: state.vehicles.map((vehicle) =>
          vehicle.uuid === action.payload.carId
            ? { ...vehicle, vehicleLocation: action.payload.location }
            : vehicle
        ),
      };
    default:
      return state;
  }
};
export default vehicleReducer;
