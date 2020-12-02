import actionTypes from "../constants/actionTypes";

const initialData = {
  availableCredit: 0,
  products: [],
  pastOrders: [],
};

const profileReducer = (state = initialData, action) => {
  switch (action.type) {
    case actionTypes.BALANCE_CHANGED:
      return {
        ...state,
        availableCredit: action.payload.availableCredit,
      };
    case actionTypes.FETCHED_PRODUCTS:
      return {
        ...state,
        products: action.payload.products,
      };
    case actionTypes.FETCHED_ORDERS:
      return {
        ...state,
        pastOrders: action.payload.orders,
      };
    case actionTypes.ORDER_RETURNED:
      return {
        ...state,
        pastOrders: state.pastOrders.map((order) =>
          order.id === action.payload.orderId ? action.payload.order : order
        ),
      };
    default:
      return state;
  }
};
export default profileReducer;
