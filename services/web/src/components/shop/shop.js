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
import { connect } from "react-redux";
import {
  PageHeader,
  Row,
  Col,
  Layout,
  Descriptions,
  Card,
  Button,
  Avatar,
  Form,
  Modal,
  Input,
} from "antd";
import {
  PlusOutlined,
  OrderedListOutlined,
  ShoppingCartOutlined,
} from "@ant-design/icons";
import { COUPON_CODE_REQUIRED } from "../../constants/messages";

const { Content } = Layout;
const { Meta } = Card;

const ProductAvatar = (product) => (
  <Avatar
    shape="square"
    className="product-avatar"
    size={250}
    src={product.image_url}
  />
);

const ProductDescription = (product, onBuyProduct) => (
  <>
    <PageHeader title={`${product.name}, $${product.price}`} />
    <Button
      type="primary"
      shape="round"
      icon={<ShoppingCartOutlined />}
      size="large"
      key="buy-product"
      className="buy-btn"
      onClick={() => onBuyProduct(product)}
    >
      Buy
    </Button>
  </>
);

const Shop = (props) => {
  const {
    products,
    availableCredit,
    history,
    isCouponFormOpen,
    setIsCouponFormOpen,
    hasErrored,
    errorMessage,
    onFinish,
  } = props;
  return (
    <Layout>
      <PageHeader
        className="page-header"
        title="Shop"
        onBack={() => history.push("/dashboard")}
        extra={[
          <Button
            type="primary"
            shape="round"
            icon={<PlusOutlined />}
            size="large"
            key="add-coupons"
            onClick={() => setIsCouponFormOpen(true)}
          >
            Add Coupons
          </Button>,
          <Button
            type="primary"
            shape="round"
            icon={<OrderedListOutlined />}
            size="large"
            onClick={() => props.history.push("/past-orders")}
            key="past-orders"
          >
            Past Orders
          </Button>,
        ]}
      />
      <Descriptions column={1} className="balance-desc">
        <Descriptions.Item label="Available Balance">
          {`$${availableCredit}`}
        </Descriptions.Item>
      </Descriptions>
      <Content>
        <Row gutter={[40, 40]}>
          {products.map((product) => (
            <Col span={8} key={product.id}>
              <Card className="product-card" cover={ProductAvatar(product)}>
                <Meta
                  description={ProductDescription(product, props.onBuyProduct)}
                />
              </Card>
            </Col>
          ))}
        </Row>
      </Content>
      <Modal
        title="Enter Coupon Code"
        visible={isCouponFormOpen}
        footer={null}
        onCancel={() => setIsCouponFormOpen(false)}
      >
        <Form
          name="basic"
          initialValues={{
            remember: true,
          }}
          onFinish={onFinish}
        >
          <Form.Item
            name="couponCode"
            rules={[{ required: true, message: COUPON_CODE_REQUIRED }]}
          >
            <Input placeholder="Coupon Code" />
          </Form.Item>
          <Form.Item>
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Validate
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

Shop.propTypes = {
  history: PropTypes.object,
  products: PropTypes.array,
  availableCredit: PropTypes.number,
  onBuyProduct: PropTypes.func,
  isCouponFormOpen: PropTypes.bool,
  setIsCouponFormOpen: PropTypes.func,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
  onFinish: PropTypes.func,
};

const mapStateToProps = ({ shopReducer: { availableCredit, products } }) => {
  return { availableCredit, products };
};

export default connect(mapStateToProps)(Shop);
