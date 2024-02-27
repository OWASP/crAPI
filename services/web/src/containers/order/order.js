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

import React, { useEffect } from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal, Avatar } from "antd";
import Order from "../../components/order/order";
import {
  getOrderByIdAction,
  returnOrderAction,
} from "../../actions/shopActions";
import responseTypes from "../../constants/responseTypes";
import { FAILURE_MESSAGE } from "../../constants/messages";

const OrderContainer = (props) => {
  const { history, accessToken, getOrderById } = props;
  const urlParams = new URLSearchParams(window.location.search);
  const orderId = urlParams.get("order_id");

  useEffect(() => {
    const callback = (res, data) => {
      if (res !== responseTypes.SUCCESS) {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    getOrderById({ callback, accessToken, orderId });
  }, [accessToken, orderId, getOrderById]);

  return <Order orderId={orderId} history={history} />;
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return { accessToken };
};

const mapDispatchToProps = {
  getOrderById: getOrderByIdAction,
};

OrderContainer.propTypes = {
  accessToken: PropTypes.string,
  getOrderById: PropTypes.func,
  history: PropTypes.object,
};

export default connect(mapStateToProps, mapDispatchToProps)(OrderContainer);
