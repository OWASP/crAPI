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

import { Modal } from "antd";
import React, { useState } from "react";
import { connect } from "react-redux";
import PropTypes from "prop-types";
import ResetPassword from "../../components/resetPassword/resetPassword";

import {
  resetPasswordAction,
  logOutUserAction,
} from "../../actions/userActions";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const ResetPasswordContainer = (props) => {
  const { history, logOutUser, resetPassword, accessToken } = props;

  const [hasErrored, setHasErrored] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const logout = () => {
    logOutUser({
      callback: () => {
        localStorage.clear();
      },
    });
  };

  const callback = (res, data) => {
    if (res === responseTypes.SUCCESS) {
      logout();
      Modal.success({
        title: SUCCESS_MESSAGE,
        content: data,
      });
    } else {
      setHasErrored(true);
      setErrorMessage(data);
    }
  };

  const onFinish = (values) => {
    resetPassword({
      ...values,
      accessToken,
      callback,
    });
  };

  return (
    <ResetPassword
      hasErrored={hasErrored}
      errorMessage={errorMessage}
      history={history}
      onFinish={onFinish}
    />
  );
};

const mapDispatchToProps = {
  resetPassword: resetPasswordAction,
  logOutUser: logOutUserAction,
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return {
    accessToken,
  };
};

ResetPasswordContainer.propTypes = {
  resetPassword: PropTypes.func,
  logOutUser: PropTypes.func,
  accessToken: PropTypes.string,
  history: PropTypes.object,
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ResetPasswordContainer);
