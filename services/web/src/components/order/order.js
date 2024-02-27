/*
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
import { RollbackOutlined } from "@ant-design/icons";
import { connect } from "react-redux";
import {
  Layout,
  Descriptions,
  PageHeader,
  Avatar,
  Row,
  Col,
  Card,
  Button,
  Typography,
  Divider,
  Modal,
  Table,
} from "antd";
import { formatDateFromIso } from "../../utils";

const { Content } = Layout;
const { Meta } = Card;

const Order = (props) => {
  const { order, history } = props;

  const renderAvatar = (url) => (
    <Avatar shape="square" className="order-avatar" size={200} src={url} />
  );

  return (
    <Layout>
      <PageHeader
        title="Order Details"
        className="page-header"
        onBack={() => history.push("/past-orders")}
      />
      <Content>
        <Row span="24">
          <Col className="order-desc" key={order && order.id} span="10">
            <Card
              className="order-card"
              cover={renderAvatar(order && order.product.image_url)}
            />
          </Col>
          <Col className="order-desc" span="10">
            <Descriptions bordered="true" column="1" layout="horizontal">
              <Descriptions.Item span="24" label="Billed To">
                {order && order.user.email}
              </Descriptions.Item>
              <Descriptions.Item span="24" label="Phone">
                {order && order.user.number}
              </Descriptions.Item>
              <Descriptions.Item span="24" label="Item">
                {order && order.product.name}
              </Descriptions.Item>
              <Descriptions.Item span="24" label="Purchased On">
                {order && `${formatDateFromIso(order.created_on)}`}
              </Descriptions.Item>
              <Descriptions.Item span="24" label="Unit Price">
                {order && `$ ${order.product.price}`}
              </Descriptions.Item>
              <Descriptions.Item span="24" label="Quantity">
                {order && order.quantity}
              </Descriptions.Item>
              <Divider span="24" />
              <Descriptions.Item span="24" label="Total">
                {order && `$ ${order.quantity * order.product.price}`}
              </Descriptions.Item>
            </Descriptions>
          </Col>
        </Row>
      </Content>
    </Layout>
  );
};

Order.propTypes = {
  history: PropTypes.object,
  order: PropTypes.object,
  returnOrder: PropTypes.func,
};

const mapStateToProps = ({ shopReducer: { order } }) => {
  return { order };
};

export default connect(mapStateToProps)(Order);
