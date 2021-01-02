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

import { connect } from "react-redux";
import PropTypes from "prop-types";
import { Button, Form, Card, Input, Select } from "antd";
import {
  VIN_REQUIRED,
  MECHANIC_REQUIRED,
  PROBLEM_REQUIRED,
} from "../../constants/messages";

const { Option } = Select;

const ContactMechanic = (props) => {
  const urlParams = new URLSearchParams(window.location.search);
  const VIN = urlParams.get("VIN");

  const { mechanics, hasErrored, errorMessage, onFinish } = props;
  return (
    <div className="container">
      <Card title="Contact Mechanic" bordered={false} className="form-card">
        <Form
          name="add-vehicle"
          initialValues={{
            remember: true,
          }}
          labelCol={{ sm: { span: 8 } }}
          wrapperCol={{ sm: { span: 16 } }}
          onFinish={onFinish}
        >
          <Form.Item
            name="vin"
            label="VIN"
            initialValue={VIN}
            rules={[{ required: true, message: VIN_REQUIRED }]}
          >
            <Input placeholder="VIN" disabled />
          </Form.Item>
          <Form.Item
            name="mechanicCode"
            label="Mechanic"
            rules={[{ required: true, message: MECHANIC_REQUIRED }]}
          >
            <Select>
              {mechanics.map((mechanic) => (
                <Option
                  value={mechanic.mechanic_code}
                  key={mechanic.mechanic_code}
                >
                  {mechanic.mechanic_code}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="problemDetails"
            label="Problem Description"
            rules={[{ required: true, message: PROBLEM_REQUIRED }]}
          >
            <Input.TextArea />
          </Form.Item>
          <Form.Item wrapperCol={{ sm: { span: 24 } }}>
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Send Service Request
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

const mapStateToProps = ({ vehicleReducer: { mechanics } }) => {
  return { mechanics };
};

ContactMechanic.propTypes = {
  mechanics: PropTypes.array,
  onFinish: PropTypes.func,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
};

export default connect(mapStateToProps)(ContactMechanic);
