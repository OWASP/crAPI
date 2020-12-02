import React from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal } from "antd";
import { verifyVehicleAction } from "../../actions/vehicleActions";
import VerifyVehicle from "../../components/verifyVehicle/verifyVehicle";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const VerifyVehicleContainer = (props) => {
  const { history, verifyVehicle } = props;

  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  const { accessToken } = props;

  const onFinish = (values) => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        Modal.success({
          title: SUCCESS_MESSAGE,
          content: data,
          onOk: () => history.push("/dashboard"),
        });
      } else {
        setHasErrored(true);
        setErrorMessage(data);
      }
    };
    verifyVehicle({
      callback,
      accessToken,
      ...values,
    });
  };

  return (
    <VerifyVehicle
      history={history}
      onFinish={onFinish}
      hasErrored={hasErrored}
      errorMessage={errorMessage}
    />
  );
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return { accessToken };
};

const mapDispatchToProps = {
  verifyVehicle: verifyVehicleAction,
};

VerifyVehicleContainer.propTypes = {
  accessToken: PropTypes.string,
  verifyVehicle: PropTypes.func,
  history: PropTypes.object,
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(VerifyVehicleContainer);
