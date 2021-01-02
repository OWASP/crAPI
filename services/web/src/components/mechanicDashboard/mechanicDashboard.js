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
