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
