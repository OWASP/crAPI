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
import { Button, Form, Input } from "antd";
import { EMAIL_REQUIRED } from "../../constants/messages";
import { EMAIL_VALIDATION } from "../../constants/constants";

const NewEmailForm = (props) => {
  const { onFinish, onMailChange, hasErrored, errorMessage } = props;
  return (
    <Form
      name="email"
      initialValues={{
        remember: true,
      }}
      onFinish={onFinish}
    >
      <Form.Item
        name="new_email"
        rules={[
          {
            required: true,
            message: EMAIL_REQUIRED,
          },
          {
            pattern: EMAIL_VALIDATION,
            message: EMAIL_REQUIRED,
          },
        ]}
      >
        <Input
          className="input-style"
          placeholder="New Email ID"
          onChange={(event) => onMailChange(event.target.value)}
        />
      </Form.Item>
      <Form.Item>
        {hasErrored && <div className="error-message">{errorMessage}</div>}
        <Button type="primary" htmlType="submit" className="form-button">
          Send Email Verification Token
        </Button>
      </Form.Item>
    </Form>
  );
};

NewEmailForm.propTypes = {
  onFinish: PropTypes.func,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
  onMailChange: PropTypes.func,
};

export default NewEmailForm;
