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

import "./styles.css";

import React from "react";
import PropTypes from "prop-types";
import { PageHeader, Row, Col, Layout, Card, Button, Avatar } from "antd";
import { RollbackOutlined } from "@ant-design/icons";
import { connect } from "react-redux";
import { formatDateFromIso } from "../../utils";

const { Content } = Layout;
const { Meta } = Card;

const PastOrders = (props) => {
  const { pastOrders, history } = props;

  const renderAvatar = (url) => (
    <Avatar shape="square" className="order-avatar" size={250} src={url} />
  );

  const renderOrderDescription = (order) => (
    <>
      <PageHeader
        title={`${formatDateFromIso(order.created_on)}, ${
          order.product.name
        }, $${order.product.price * order.quantity}`}
        extra={[
          <Button
            type="primary"
            shape="round"
            icon={order.status === "delivered" && <RollbackOutlined />}
            size="large"
            key="return-order"
            disabled={order.status !== "delivered"}
            onClick={() => props.returnOrder(order.id)}
          >
            {order.status === "delivered" ? "Return" : order.status}
          </Button>,
        ]}
      />
    </>
  );

  return (
    <Layout>
      <PageHeader
        title="Past Orders"
        className="page-header"
        onBack={() => history.push("/shop")}
      />
      <Content>
        <Row gutter={[40, 40]}>
          {pastOrders.map((order) => (
            <Col span={8} key={order.id}>
              <Card
                className="order-card"
                cover={renderAvatar(order.product.image_url)}
              >
                <Meta description={renderOrderDescription(order)} />
              </Card>
            </Col>
          ))}
        </Row>
      </Content>
    </Layout>
  );
};

PastOrders.propTypes = {
  history: PropTypes.object,
  pastOrders: PropTypes.array,
  returnOrder: PropTypes.func,
};

const mapStateToProps = ({ shopReducer: { pastOrders } }) => {
  return { pastOrders };
};

export default connect(mapStateToProps)(PastOrders);
