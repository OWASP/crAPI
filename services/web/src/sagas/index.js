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

import { all } from "redux-saga/effects";
import { userActionWatcher } from "./userSaga";
import { shopActionWatcher } from "./shopSaga";
import { profileActionWatcher } from "./profileSaga";
import { communityActionWatcher } from "./communitySaga";
import { vehicleActionWatcher } from "./vehicleSaga";

/**
 * saga to yield all others
 */
export default function* rootSaga() {
  yield all([
    userActionWatcher(),
    shopActionWatcher(),
    profileActionWatcher(),
    communityActionWatcher(),
    vehicleActionWatcher(),
  ]);
}
