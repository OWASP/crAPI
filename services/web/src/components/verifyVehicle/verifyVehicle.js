import React from "react";
import PropTypes from "prop-types";
import { Button, Form, Card, Input } from "antd";
import {
  PIN_CODE_REQUIRED,
  INVALID_PIN_CODE,
  VEHICLE_NO_REQUIRED,
  INVALID_VIN,
} from "../../constants/messages";
import { PIN_CODE_VALIDATION, VIN_VALIDATION } from "../../constants/constants";

const VerifyVehicle = (props) => {
  const { hasErrored, errorMessage, onFinish } = props;
  return (
    <div className="container">
      <Card
        title="Verify Vehicle Details"
        bordered={false}
        className="form-card"
      >
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
            name="pinCode"
            label="Pin Code"
            rules={[
              { required: true, message: PIN_CODE_REQUIRED },
              { pattern: PIN_CODE_VALIDATION, message: INVALID_PIN_CODE },
            ]}
          >
            <Input placeholder="Pin Code" />
          </Form.Item>
          <Form.Item
            name="vin"
            label="VIN"
            rules={[
              { required: true, message: VEHICLE_NO_REQUIRED },
              { pattern: VIN_VALIDATION, message: INVALID_VIN },
            ]}
          >
            <Input placeholder="Vehicle No." />
          </Form.Item>
          <Form.Item wrapperCol={{ sm: { span: 24 } }}>
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Verify Vehicle Details
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

VerifyVehicle.propTypes = {
  onFinish: PropTypes.func,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
};

export default VerifyVehicle;
