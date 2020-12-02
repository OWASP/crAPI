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
