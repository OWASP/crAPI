import React, { useState } from "react";
import { Modal } from "antd";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import Signup from "../../components/signup/signup";

import { signUpUserAction } from "../../actions/userActions";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const SignupContainer = (props) => {
  const { history, signUpUser } = props;

  const [hasErrored, setHasErrored] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const callback = (res, data) => {
    if (res === responseTypes.SUCCESS) {
      Modal.success({
        title: SUCCESS_MESSAGE,
        content: data,
        onOk: () => history.push("/login"),
      });
    } else {
      setHasErrored(true);
      setErrorMessage(data);
    }
  };

  const onFinish = (values) => {
    signUpUser({ ...values, callback });
  };

  return (
    <Signup
      history={history}
      hasErrored={hasErrored}
      errorMessage={errorMessage}
      onFinish={onFinish}
    />
  );
};

const mapDispatchToProps = {
  signUpUser: signUpUserAction,
};

SignupContainer.propTypes = {
  signUpUser: PropTypes.func,
  history: PropTypes.object,
};

export default connect(null, mapDispatchToProps)(SignupContainer);
