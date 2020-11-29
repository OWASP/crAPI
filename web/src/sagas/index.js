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
