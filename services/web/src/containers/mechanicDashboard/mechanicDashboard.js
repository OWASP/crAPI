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

import React, { useState, useEffect } from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal } from "antd";
import { getServicesAction } from "../../actions/userActions";
import MechanicDashboard from "../../components/mechanicDashboard/mechanicDashboard";
import responseTypes from "../../constants/responseTypes";
import { FAILURE_MESSAGE } from "../../constants/messages";

const MechanicDashboardContainer = (props) => {
  const { history, accessToken, getServices } = props;

  const [services, setServices] = useState([]);

  useEffect(() => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        setServices(data);
      } else {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    getServices({ callback, accessToken });
  }, [accessToken, getServices]);

  return <MechanicDashboard history={history} services={services} />;
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return { accessToken };
};

const mapDispatchToProps = {
  getServices: getServicesAction,
};

MechanicDashboardContainer.propTypes = {
  accessToken: PropTypes.string,
  getServices: PropTypes.func,
  history: PropTypes.object
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(MechanicDashboardContainer);
