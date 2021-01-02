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

import "./dashboard.css";

import { connect } from "react-redux";
import React from "react";
import PropTypes from "prop-types";
import {
  PageHeader,
  Card,
  Row,
  Col,
  Button,
  Avatar,
  Descriptions,
  Layout,
  Alert,
} from "antd";
import { ToolOutlined, SyncOutlined, PlusOutlined } from "@ant-design/icons";
import { getMapUrl } from "../../utils";
import {
  NO_VEHICLE_DESC_1,
  NO_VEHICLE_DESC_2,
  NO_VEHICLE_DESC_3,
} from "../../constants/messages";

const { Meta } = Card;
const { Content } = Layout;

const vehicleCardHeader = (vehicle, history) => (
  <PageHeader
    className="dashboard-header"
    title={`VIN: ${vehicle.vin}`}
    extra={[
      <Button
        type="primary"
        shape="round"
        icon={<ToolOutlined />}
        size="large"
        onClick={() => history.push(`/contact-mechanic?VIN=${vehicle.vin}`)}
        key="add-vehicle"
      >
        Contact Mechanic
      </Button>,
    ]}
  />
);

const Dashboard = (props) => {
  const { vehicles, history, refreshLocation, resendMail } = props;

  const vehicleCardContent = (vehicle) => (
    <>
      <Row gutter={[20, 20]}>
        <Col flex="420px">
          <Avatar shape="square" size={400} src={vehicle.model.vehicle_img} />
        </Col>
        <Col flex="auto">
          <Descriptions
            size="large"
            column={1}
            className="vehicle-desc"
            bordered
          >
            <Descriptions.Item label="Company :">
              {vehicle.model.vehiclecompany.name}
            </Descriptions.Item>
            <Descriptions.Item label="Model :">
              {vehicle.model.model}
            </Descriptions.Item>
            <Descriptions.Item label="Fuel Type :">
              {vehicle.model.fuel_type}
            </Descriptions.Item>
            <Descriptions.Item label="Year :">{vehicle.year}</Descriptions.Item>
          </Descriptions>
        </Col>
        <Col flex="auto">
          <Row>
            <Col span={24}>
              <iframe
                className="map-iframe"
                width="100%"
                height="360"
                src={getMapUrl(
                  vehicle.vehicleLocation.latitude,
                  vehicle.vehicleLocation.longitude
                )}
                title="Map"
              />
            </Col>
            <Col span={24}>
              <Button
                type="primary"
                shape="round"
                icon={<SyncOutlined />}
                size="large"
                // className="refresh-loc-btn"
                onClick={() => refreshLocation(vehicle.uuid)}
              >
                Refresh Location
              </Button>
            </Col>
          </Row>
        </Col>
      </Row>
    </>
  );

  const renderNoVehicleDescription = () => (
    <>
      <span className="alert-msg">{NO_VEHICLE_DESC_1}</span>
      <button onClick={resendMail} type="button" className="alert-msg btn">
        {NO_VEHICLE_DESC_2}
      </button>
      <span className="alert-msg">{NO_VEHICLE_DESC_3}</span>
    </>
  );

  return (
    <Layout className="page-container">
      <PageHeader
        className="dashboard-header"
        title="Vehicles Details"
        extra={
          !vehicles.length && [
            <Button
              type="primary"
              shape="round"
              icon={<PlusOutlined />}
              size="large"
              onClick={() => history.push("/verify-vehicle")}
              key="verify-vehicle"
            >
              Add a Vehicle
            </Button>,
          ]
        }
      />
      <Content>
        <Row gutter={[40, 40]}>
          {vehicles.map((vehicle) => (
            <Col span={24} key={vehicle.vin}>
              <Card className="vehicle-card">
                <Meta
                  title={vehicleCardHeader(vehicle, history)}
                  description={vehicleCardContent(vehicle)}
                />
              </Card>
            </Col>
          ))}
          {!vehicles.length && (
            <Col className="alert-msg-box">
              <Alert
                message={
                  <span className="alert-header">No Vehicles Found</span>
                }
                description={renderNoVehicleDescription()}
                type="warning"
              />
            </Col>
          )}
        </Row>
      </Content>
    </Layout>
  );
};

const mapStateToProps = ({ vehicleReducer: { vehicles } }) => {
  return { vehicles };
};

Dashboard.propTypes = {
  history: PropTypes.object,
  vehicles: PropTypes.array,
  resendMail: PropTypes.func,
  refreshLocation: PropTypes.func,
};

export default connect(mapStateToProps)(Dashboard);
