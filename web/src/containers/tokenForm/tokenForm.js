import React from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal } from "antd";
import { verifyTokenAction, logOutUserAction } from "../../actions/userActions";
import TokenForm from "../../components/tokenForm/tokenForm";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE } from "../../constants/messages";

const TokenFormContainer = (props) => {
  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  const callback = (res, data) => {
    if (res === responseTypes.SUCCESS) {
      Modal.success({
        title: SUCCESS_MESSAGE,
        content: data,
        onOk: () => {
          props.logOutUser({
            callback: () => {
              localStorage.clear();
              if (!localStorage.getItem("token")) props.history.push("/login");
            },
          });
        },
      });
    } else {
      setHasErrored(true);
      setErrorMessage(data);
    }
  };

  const onFinish = (values) => {
    const { accessToken } = props;
    props.verifyToken({
      ...values,
      new_email: props.email,
      callback,
      accessToken,
    });
  };

  return (
    <TokenForm
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
  verifyToken: verifyTokenAction,
  logOutUser: logOutUserAction,
};

TokenFormContainer.propTypes = {
  accessToken: PropTypes.string,
  verifyToken: PropTypes.func,
  email: PropTypes.string,
  history: PropTypes.object,
  logOutUser: PropTypes.func,
};

export default connect(mapStateToProps, mapDispatchToProps)(TokenFormContainer);
