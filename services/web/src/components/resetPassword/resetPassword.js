import { Button, Form, Input, Card } from "antd";
import React from "react";

import PropTypes from "prop-types";

import {
  PASSWORD_REQUIRED,
  CONFIRM_PASSWORD,
  PASSWORD_DO_NOT_MATCH,
  INVALID_PASSWORD,
} from "../../constants/messages";
import { PASSWORD_VALIDATION } from "../../constants/constants";

const ResetPassword = (props) => {
  const { hasErrored, errorMessage, onFinish } = props;

  return (
    <div className="container">
      <Card title="Reset Password" bordered={false} className="form-card">
        <Form
          name="basic"
          initialValues={{
            remember: true,
          }}
          onFinish={onFinish}
        >
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
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Reset Password
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

ResetPassword.propTypes = {
  history: PropTypes.object,
  onFinish: PropTypes.func,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
};

export default ResetPassword;
