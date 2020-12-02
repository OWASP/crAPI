import { combineReducers } from "redux";
import userReducer from "./userReducer";
import profileReducer from "./profileReducer";
import vehicleReducer from "./vehicleReducer";
import shopReducer from "./shopReducer";
import communityReducer from "./communityReducer";

const appReducer = combineReducers({
  userReducer,
  profileReducer,
  vehicleReducer,
  shopReducer,
  communityReducer,
});

const rootReducer = (state, action) => {
  return appReducer(state, action);
};

export default rootReducer;
