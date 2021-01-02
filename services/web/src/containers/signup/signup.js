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
