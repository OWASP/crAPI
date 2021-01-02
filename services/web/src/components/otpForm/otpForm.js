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

import { Button, Form, Input } from "antd";

import React from "react";

import PropTypes from "prop-types";
import {
  OTP_REQUIRED,
  PASSWORD_REQUIRED,
  CONFIRM_PASSWORD,
  PASSWORD_DO_NOT_MATCH,
  INVALID_PASSWORD,
} from "../../constants/messages";
import { PASSWORD_VALIDATION } from "../../constants/constants";

const OtpForm = (props) => {
  const { onFinish, errorMessage, hasErrored } = props;
  return (
    <Form
      name="password"
      initialValues={{
        remember: true,
      }}
      onFinish={onFinish}
    >
      <Form.Item
        name="otp"
        rules={[
          {
            required: true,
            message: OTP_REQUIRED,
          },
        ]}
      >
        <Input className="input-style" placeholder="OTP" />
      </Form.Item>
      <Form.Item
        name="password"
        rules={[
          {
            required: true,
            message: PASSWORD_REQUIRED,
          },
          {
            pattern: PASSWORD_VALIDATION,
            message: INVALID_PASSWORD,
          },
        ]}
      >
        <Input.Password className="input-style" placeholder="Password" />
      </Form.Item>
      <Form.Item
        name="againPassword"
        dependencies={["password"]}
        rules={[
          {
            required: true,
            message: CONFIRM_PASSWORD,
          },
          ({ getFieldValue }) => ({
            validator(rule, value) {
              if (!value || getFieldValue("password") === value) {
                return Promise.resolve();
              }
              return Promise.reject(PASSWORD_DO_NOT_MATCH);
            },
          }),
        ]}
      >
        <Input.Password
          className="input-style"
          placeholder="Re-enter Password"
        />
      </Form.Item>
      <Form.Item>
        {hasErrored && <div className="error-message">{errorMessage}</div>}
        <Button type="primary" htmlType="submit" className="form-button">
          Set Password
        </Button>
      </Form.Item>
    </Form>
  );
};

OtpForm.propTypes = {
  onFinish: PropTypes.func,
  errorMessage: PropTypes.string,
  hasErrored: PropTypes.bool,
};

export default OtpForm;
