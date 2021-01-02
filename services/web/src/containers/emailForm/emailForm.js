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
import React from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { forgotPasswordAction } from "../../actions/userActions";
import EmailForm from "../../components/emailForm/emailForm";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const EmailFormContainer = (props) => {
  const { onMailChange } = props;

  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  const callback = (res, data) => {
    if (res === responseTypes.SUCCESS) {
      Modal.success({
        title: SUCCESS_MESSAGE,
        content: data,
        onOk: () => props.setCurrentStep(props.currentStep + 1),
      });
    } else {
      setHasErrored(true);
      setErrorMessage(data);
    }
  };

  const onFinish = (values) => {
    props.forgotPassword({ ...values, callback });
  };

  return (
    <EmailForm
      onFinish={onFinish}
      hasErrored={hasErrored}
      errorMessage={errorMessage}
      onMailChange={onMailChange}
    />
  );
};

const mapDispatchToProps = {
  forgotPassword: forgotPasswordAction,
};

EmailFormContainer.propTypes = {
  forgotPassword: PropTypes.func,
  currentStep: PropTypes.number,
  setCurrentStep: PropTypes.func,
  onMailChange: PropTypes.func,
  history: PropTypes.object
};

export default connect(null, mapDispatchToProps)(EmailFormContainer);
