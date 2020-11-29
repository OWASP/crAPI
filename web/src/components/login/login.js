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
