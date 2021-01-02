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
import { changeEmailAction } from "../../actions/userActions";
import NewEmailForm from "../../components/newEmailForm/newEmailForm";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const NewEmailFormContainer = (props) => {
  const { accessToken, oldEmail, onMailChange } = props;

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
    props.changeEmail({
      ...values,
      callback,
      accessToken,
      old_email: oldEmail,
    });
  };

  return (
    <NewEmailForm
      onFinish={onFinish}
      hasErrored={hasErrored}
      errorMessage={errorMessage}
      onMailChange={onMailChange}
    />
  );
};

const mapStateToProps = ({ userReducer: { accessToken, email } }) => {
  return { accessToken, oldEmail: email };
};

const mapDispatchToProps = {
  changeEmail: changeEmailAction,
};

NewEmailFormContainer.propTypes = {
  oldEmail: PropTypes.string,
  accessToken: PropTypes.string,
  changeEmail: PropTypes.func,
  currentStep: PropTypes.number,
  setCurrentStep: PropTypes.func,
  onMailChange: PropTypes.func,
  history: PropTypes.object,
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(NewEmailFormContainer);
