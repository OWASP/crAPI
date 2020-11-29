import { invalidSessionAction } from "../actions/userActions";

export const authInterceptor = ({dispatch}) => (next) => (action) => {
	console.log(action);
	if (action.payload?.status === 401) {
		console.log("Logging out");
		return dispatch(invalidSessionAction());
	}
	if (!action.error) {
		return next(action);
	}
	return next(action);
};