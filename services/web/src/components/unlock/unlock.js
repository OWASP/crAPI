/*
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

import "./unlock.css";

import { Button, Form, Input, Card } from "antd";
import React from "react";
import PropTypes from "prop-types";

import { UserOutlined } from "@ant-design/icons";
import { EMAIL_VALIDATION } from "../../constants/constants";
import { EMAIL_REQUIRED } from "../../constants/messages";

/**
 * unlock component for users
 */
const Unlock = (props) => {
  const { email, message, hasErrored, errorMessage, onFinish, history } = props;

  return (
    <div className="container">
      <Card title="Unlock Account" bordered={false} className="form-card">
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
            <Input
              placeholder="Email"
              prefix={<UserOutlined />}
              value={email}
            />
          </Form.Item>
          <Form.Item
            name="code"
            rules={[
              {
                required: true,
                message: "Please input your code received on email!",
              },
            ]}
          >
            <Input.Password placeholder="Code" />
          </Form.Item>
          <Form.Item>
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            {message && <div className="error-message">{message}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Unlock
            </Button>
          </Form.Item>
          <Form.Item>
            <button
              className="alternative-style"
              onClick={() => history.push("/login")}
              type="button"
            >
              {" "}
              Already Unlocked? Login
            </button>
          </Form.Item>
          <Form.Item>
            <button
              className="alternative-style"
              onClick={() => history.push("/signup")}
              type="button"
            >
              {" "}
              Dont have an Account? SignUp
            </button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

Unlock.propTypes = {
  email: PropTypes.string,
  message: PropTypes.string,
  history: PropTypes.object,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
  onFinish: PropTypes.func,
};

Unlock.defaultProps = {
  hasErrored: false,
  message: "",
  errorMessage: "",
};

export default Unlock;
