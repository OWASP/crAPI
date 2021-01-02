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

import "./login.css";

import { Button, Form, Input, Card } from "antd";
import React from "react";
import PropTypes from "prop-types";

import { UserOutlined } from "@ant-design/icons";
import { EMAIL_VALIDATION } from "../../constants/constants";
import { EMAIL_REQUIRED } from "../../constants/messages";

/**
 * login component for users
 */
const Login = (props) => {
  const { hasErrored, errorMessage, onFinish, history } = props;

  return (
    <div className="container">
      <Card title="Login" bordered={false} className="form-card">
        <Form
          name="basic"
          initialValues={{
            remember: true,
          }}
          onFinish={onFinish}
        >
          <Form.Item
            name="email"
            rules={[
              { required: true, message: EMAIL_REQUIRED },
              {
                pattern: EMAIL_VALIDATION,
                message: EMAIL_REQUIRED,
              },
            ]}
          >
            <Input placeholder="Email" prefix={<UserOutlined />} />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[
              {
                required: true,
                message: "Please input your password!",
              },
            ]}
          >
            <Input.Password placeholder="Password" />
          </Form.Item>
          <Form.Item>
            <button
              className="alternative-style"
              onClick={() => history.push("/forgot-password")}
              type="button"
            >
              Forgot Password?
            </button>
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Login
            </Button>
            <button
              className="alternative-style"
              onClick={() => history.push("/signup")}
              type="button"
            >
              Dont have an Account? SignUp
            </button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

Login.propTypes = {
  history: PropTypes.object,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
  onFinish: PropTypes.func,
};

Login.defaultProps = {
  hasErrored: false,
  errorMessage: "",
};

export default Login;
