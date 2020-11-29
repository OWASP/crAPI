import React from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal } from "antd";
import NewPost from "../../components/newPost/newPost";
import { addPostAction } from "../../actions/communityActions";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const NewPostContainer = (props) => {
  const { history, accessToken } = props;

  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  const onFinish = (values) => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        Modal.success({
          title: SUCCESS_MESSAGE,
          content: data,
          onOk: () => history.push("/forum"),
        });
      } else {
        setHasErrored(true);
        setErrorMessage(data);
      }
    };
    props.addPost({
      callback,
      accessToken,
      post: {
        ...values,
      },
    });
  };

  return (
    <NewPost
      history={history}
      onFinish={onFinish}
      hasErrored={hasErrored}
      errorMessage={errorMessage}
    />
  );
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return { accessToken };
};

const mapDispatchToProps = {
  addPost: addPostAction,
};

NewPostContainer.propTypes = {
  accessToken: PropTypes.string,
  addPost: PropTypes.func,
  history: PropTypes.object,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewPostContainer);
