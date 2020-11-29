import React, { useEffect } from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal, Avatar } from "antd";
import { getOrdersAction, returnOrderAction } from "../../actions/shopActions";
import PastOrders from "../../components/pastOrders/pastOrders";
import responseTypes from "../../constants/responseTypes";
import { FAILURE_MESSAGE } from "../../constants/messages";

const PastOrdersContainer = (props) => {
  const { history, accessToken, getOrders, returnOrder } = props;

  useEffect(() => {
    const callback = (res, data) => {
      if (res !== responseTypes.SUCCESS) {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    getOrders({ callback, accessToken });
  }, [accessToken, getOrders]);

  const handleReturnOrder = (orderId) => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        Modal.success({
          title: data.message,
          content: (
            <Avatar
              shape="square"
              src={data.qr_code_url}
              alt="Return QR Code"
              size={200}
            />
          ),
          onOk: () => history.push("/past-orders"),
        });
      } else {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    returnOrder({ callback, accessToken, orderId });
  };

  return <PastOrders history={history} returnOrder={handleReturnOrder} />;
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return { accessToken };
};

const mapDispatchToProps = {
  getOrders: getOrdersAction,
  returnOrder: returnOrderAction,
};

PastOrdersContainer.propTypes = {
  accessToken: PropTypes.string,
  getOrders: PropTypes.func,
  returnOrder: PropTypes.func,
  history: PropTypes.object,
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(PastOrdersContainer);
