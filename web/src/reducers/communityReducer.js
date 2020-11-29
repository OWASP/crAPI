import actionTypes from "../constants/actionTypes";

const initialData = {
  posts: [],
};

const communityReducer = (state = initialData, action) => {
  switch (action.type) {
    case actionTypes.FETCHED_POSTS:
      return {
        ...state,
        posts: action.payload,
      };
    case actionTypes.FETCHED_POST:
      return {
        ...state,
        posts: state.posts.map((post) =>
          post.id === action.payload.postId ? action.payload.post : post
        ),
        post: action.payload.post,
      };
    default:
      return state;
  }
};
export default communityReducer;
