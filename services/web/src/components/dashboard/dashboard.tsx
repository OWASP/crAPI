/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import "./dashboard.css";

import { connect, ConnectedProps } from "react-redux";
import React from "react";
import {
  Card,
  Row,
  Col,
  Button,
  Avatar,
  Descriptions,
  Layout,
  Alert,
} from "antd";

import { PageHeader } from "@ant-design/pro-components";
import { ToolOutlined, SyncOutlined, PlusOutlined } from "@ant-design/icons";
import { getMapUrl } from "../../utils";
import {
  NO_VEHICLE_DESC_1,
  NO_VEHICLE_DESC_2,
  NO_VEHICLE_DESC_3,
} from "../../constants/messages";
import { useNavigate } from "react-router-dom";

const { Meta } = Card;
const { Content } = Layout;

interface Vehicle {
  vin: string;
  uuid: string;
  year: number;
  model: {
    vehicle_img: string;
    vehiclecompany: {
      name: string;
    };
    model: string;
    fuel_type: string;
  };
  vehicleLocation: {
    latitude: number;
    longitude: number;
  };
}

interface RootState {
  vehicleReducer: {
    vehicles: Vehicle[];
  };
}

const vehicleCardHeader = (
  vehicle: Vehicle,
  handleVehicleServiceClick: (vin: string) => void,
  handleContactMechanic: (vin: string) => void,
) => {
  return (
    <PageHeader
      className="dashboard-header"
      title={`VIN: ${vehicle.vin}`}
      extra={[
        <Button
          type="primary"
          shape="round"
          icon={<ToolOutlined />}
          size="large"
          onClick={() => handleVehicleServiceClick(vehicle.vin)}
          key="vehicle-service-history"
        >
          Vehicle Service History
        </Button>,
        <Button
          type="primary"
          shape="round"
          icon={<ToolOutlined />}
          size="large"
          onClick={() => handleContactMechanic(vehicle.vin)}
          key="contact-mechanic"
        >
          Contact Mechanic
        </Button>,
      ]}
    />
  );
};

const connector = connect((state: RootState) => ({
  vehicles: state.vehicleReducer.vehicles,
}));

type PropsFromRedux = ConnectedProps<typeof connector>;

interface DashboardProps extends PropsFromRedux {
  refreshLocation: (uuid: string) => void;
  resendMail: () => void;
}

const Dashboard: React.FC<DashboardProps> = ({
  vehicles,
  refreshLocation,
  resendMail,
}) => {
  const navigate = useNavigate();
  const vehicleCardContent = (vehicle: Vehicle) => (
    <>
      <Row gutter={[10, 10]}>
        <Col flex="30%" style={{ margin: "auto" }}>
          <Avatar
            shape="square"
            size={{ xs: 200, sm: 229, md: 240, lg: 260, xl: 280, xxl: 300 }}
            src={vehicle.model.vehicle_img}
          />
        </Col>
        <Col flex="60%" style={{ margin: "auto" }}>
          <Descriptions
            size="middle"
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
                height="200"
                src={getMapUrl(
                  vehicle.vehicleLocation.latitude,
                  vehicle.vehicleLocation.longitude,
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
  const handleVerifyVehicleClick = () => {
    navigate("/verify-vehicle");
  };

  const handleVehicleServiceClick = (vin: string) => {
    navigate(`/vehicle-service-dashboard?VIN=${vin}`);
  };

  const handleContactMechanic = (vin: string) => {
    navigate(`/contact-mechanic?VIN=${vin}`);
  };
  return (
    <Layout className="page-container">
      <PageHeader
        className="dashboard-header"
        title="Vehicles Details"
        extra={[
          <Button
            type="primary"
            shape="round"
            icon={<PlusOutlined />}
            size="large"
            onClick={handleVerifyVehicleClick}
            key="verify-vehicle"
          >
            Add Vehicle
          </Button>,
        ]}
      />
      <Content>
        <Row gutter={[40, 40]}>
          {vehicles.map((vehicle) => (
            <Col span={24} key={vehicle.vin}>
              <Card className="vehicle-card">
                {vehicleCardHeader(
                  vehicle,
                  handleVehicleServiceClick,
                  handleContactMechanic,
                )}
                {vehicleCardContent(vehicle)}
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

export default connector(Dashboard);
