import { Button, Form, Input } from "antd";

import React from "react";

import PropTypes from "prop-types";
import { TOKEN_REQUIRED } from "../../constants/messages";

const TokenForm = (props) => {
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
        name="token"
        rules={[
          {
            required: true,
            message: TOKEN_REQUIRED,
          },
        ]}
      >
        <Input className="input-style" placeholder="Token" />
      </Form.Item>
      <Form.Item>
        {hasErrored && <div className="error-message">{errorMessage}</div>}
        <Button type="primary" htmlType="submit" className="form-button">
          Change Email
        </Button>
      </Form.Item>
    </Form>
  );
};

TokenForm.propTypes = {
  onFinish: PropTypes.func,
  errorMessage: PropTypes.string,
  hasErrored: PropTypes.bool,
};

export default TokenForm;
