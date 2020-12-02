import React from "react";
import PropTypes from "prop-types";
import { PageHeader, Card, Row, Col } from "antd";

const { Meta } = Card;

const MechanicDashboard = (props) => {
  const { services } = props;
  return (
    <>
      <PageHeader title="Pending Services" />
      <Row gutter={[16, 24]}>
        {services.map((service) => (
          <Col span={8} key={service.id}>
            <Card hoverable className="dashboard-card">
              <Meta
                title={service.problem_details}
                description={service.created_on}
              />
              <p>
                Owner email-id:
                {service.vehicle.owner.email}
              </p>
              <p>
                Owner Phone No.:
                {service.vehicle.owner.number}
              </p>
            </Card>
          </Col>
        ))}
      </Row>
    </>
  );
};

MechanicDashboard.propTypes = {
  history: PropTypes.object,
  services: PropTypes.array,
};

export default MechanicDashboard;
