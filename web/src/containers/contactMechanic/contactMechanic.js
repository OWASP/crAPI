import React, { useEffect } from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal } from "antd";
import ContactMechanic from "../../components/contactMechanic/contactMechanic";
import {
  getMechanicsAction,
  contactMechanicAction,
} from "../../actions/vehicleActions";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const ContactMechanicContainer = (props) => {
  const { history, accessToken, getMechanics } = props;

  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  useEffect(() => {
    const callback = (res, data) => {
      if (res !== responseTypes.SUCCESS) {
        setHasErrored(true);
        setErrorMessage(data);
      }
    };
    getMechanics({ callback, accessToken });
  }, [accessToken, getMechanics]);

  const onFinish = (values) => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        Modal.success({
          title: SUCCESS_MESSAGE,
          content: data,
          onOk: () => history.push("/mechanic-dashboard"),
        });
      } else {
        setHasErrored(true);
        setErrorMessage(data);
      }
    };
    props.contactMechanic({
      callback,
      accessToken,
      ...values,
    });
  };

  return (
    <ContactMechanic
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
  getMechanics: getMechanicsAction,
  contactMechanic: contactMechanicAction,
};

ContactMechanicContainer.propTypes = {
  accessToken: PropTypes.string,
  getMechanics: PropTypes.func,
  contactMechanic: PropTypes.func,
  history: PropTypes.object,
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ContactMechanicContainer);
