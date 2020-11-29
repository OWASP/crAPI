import { Button, Form, Input, Card } from "antd";
import React from "react";

import PropTypes from "prop-types";
import {
  EMAIL_REQUIRED,
  NAME_REQUIRED,
  INVALID_PHONE,
  PASSWORD_REQUIRED,
  CONFIRM_PASSWORD,
  PASSWORD_DO_NOT_MATCH,
  INVALID_PASSWORD,
} from "../../constants/messages";
import {
  EMAIL_VALIDATION,
  NAME_VALIDATION,
  PHONE_VALIDATION,
  PASSWORD_VALIDATION,
} from "../../constants/constants";

const Signup = (props) => {
  const { history, hasErrored, errorMessage, onFinish } = props;

  return (
    <div className="container">
      <Card title="Sign Up" bordered={false} className="form-card">
        <Form
          name="basic"
          initialValues={{
            remember: true,
          }}
          onFinish={onFinish}
        >
          <Form.Item
            name="name"
            rules={[
              { required: true, message: NAME_REQUIRED },
              {
                pattern: NAME_VALIDATION,
                message: NAME_REQUIRED,
              },
            ]}
          >
            <Input placeholder="Full Name" />
          </Form.Item>
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
            <Input placeholder="Email" />
          </Form.Item>
          <Form.Item
            name="number"
            rules={[
              {
                pattern: PHONE_VALIDATION,
                message: INVALID_PHONE,
              },
            ]}
          >
            <Input placeholder="Phone No." />
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
            <Input.Password placeholder="Password" />
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
            <Input.Password placeholder="Re-enter Password" />
          </Form.Item>
          <Form.Item>
            <button
              className="alternative-style"
              onClick={() => history.push("/login")}
              type="button"
            >
              Already have an Account? Login
            </button>
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Signup
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

Signup.propTypes = {
  onFinish: PropTypes.func,
  history: PropTypes.object,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
};

Signup.defaultProps = {
  hasErrored: false,
  errorMessage: "",
};

export default Signup;
