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
