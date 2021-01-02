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
import { verifyOTPAction } from "../../actions/userActions";
import OTPForm from "../../components/otpForm/otpForm";
import responseTypes from "../../constants/responseTypes";
import { FAILURE_MESSAGE, SUCCESS_MESSAGE } from "../../constants/messages";

const OtpFormContainer = (props) => {
  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  const callback = (res, data) => {
    if (res === responseTypes.SUCCESS) {
      Modal.success({
        title: SUCCESS_MESSAGE,
        content: data,
        onOk: () => props.history.push("/login"),
      });
    } else if (res === responseTypes.REDIRECT) {
      Modal.error({
        title: FAILURE_MESSAGE,
        content: data,
        onOk: () => props.history.push("/login"),
        onCancel: () => props.history.push("/login"),
      });
    } else {
      setHasErrored(true);
      setErrorMessage(data);
    }
  };

  const onFinish = (values) => {
    props.verifyOTP({ ...values, email: props.email, callback });
  };

  return (
    <OTPForm
      onFinish={onFinish}
      hasErrored={hasErrored}
      errorMessage={errorMessage}
    />
  );
};

const mapDispatchToProps = {
  verifyOTP: verifyOTPAction,
};

OtpFormContainer.propTypes = {
  verifyOTP: PropTypes.func,
  email: PropTypes.string,
  history: PropTypes.object
};

export default connect(null, mapDispatchToProps)(OtpFormContainer);
