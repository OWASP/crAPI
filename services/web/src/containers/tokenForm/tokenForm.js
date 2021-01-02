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

import React from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal } from "antd";
import { verifyTokenAction, logOutUserAction } from "../../actions/userActions";
import TokenForm from "../../components/tokenForm/tokenForm";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const TokenFormContainer = (props) => {
  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  const callback = (res, data) => {
    if (res === responseTypes.SUCCESS) {
      Modal.success({
        title: SUCCESS_MESSAGE,
        content: data,
        onOk: () => {
          props.logOutUser({
            callback: () => {
              localStorage.clear();
              if (!localStorage.getItem("token")) props.history.push("/login");
            },
          });
        },
      });
    } else {
      setHasErrored(true);
      setErrorMessage(data);
    }
  };

  const onFinish = (values) => {
    const { accessToken } = props;
    props.verifyToken({
      ...values,
      new_email: props.email,
      callback,
      accessToken,
    });
  };

  return (
    <TokenForm
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
  verifyToken: verifyTokenAction,
  logOutUser: logOutUserAction,
};

TokenFormContainer.propTypes = {
  accessToken: PropTypes.string,
  verifyToken: PropTypes.func,
  email: PropTypes.string,
  history: PropTypes.object,
  logOutUser: PropTypes.func,
};

export default connect(mapStateToProps, mapDispatchToProps)(TokenFormContainer);
