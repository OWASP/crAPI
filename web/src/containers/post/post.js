import React, { useState, useEffect } from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal } from "antd";
import Post from "../../components/post/post";
import {
  getPostByIdAction,
  addCommentAction,
} from "../../actions/communityActions";
import responseTypes from "../../constants/responseTypes";
import { FAILURE_MESSAGE, SUCCESS_MESSAGE } from "../../constants/messages";

const PostContainer = (props) => {
  const { history, accessToken, getPostById, addComment } = props;

  const [isCommentFormOpen, setIsCommentFormOpen] = useState(false);

  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  const urlParams = new URLSearchParams(window.location.search);
  const postId = urlParams.get("post_id");

  useEffect(() => {
    const callback = (res, data) => {
      if (res !== responseTypes.SUCCESS) {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    getPostById({ callback, accessToken, postId });
  }, [accessToken, postId, getPostById]);

  const onFinish = (values) => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        Modal.success({
          title: SUCCESS_MESSAGE,
          content: data,
        });
        setIsCommentFormOpen(false);
      } else {
        setHasErrored(true);
        setErrorMessage(data);
      }
    };
    addComment({
      callback,
      accessToken,
      postId,
      ...values,
    });
  };

  return (
    <Post
      history={history}
      onFinish={onFinish}
      hasErrored={hasErrored}
      errorMessage={errorMessage}
      isCommentFormOpen={isCommentFormOpen}
      setIsCommentFormOpen={setIsCommentFormOpen}
    />
  );
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return { accessToken };
};

const mapDispatchToProps = {
  getPostById: getPostByIdAction,
  addComment: addCommentAction,
};

PostContainer.propTypes = {
  accessToken: PropTypes.string,
  getPostById: PropTypes.func,
  addComment: PropTypes.func,
  history: PropTypes.object,
};

export default connect(mapStateToProps, mapDispatchToProps)(PostContainer);
