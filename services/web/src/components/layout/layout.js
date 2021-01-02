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

import "./layout.css";

import React, { useState } from "react";

import { Redirect, Route, Switch } from "react-router-dom";

import PropTypes from "prop-types";
import { Layout, Spin } from "antd";
import { connect } from "react-redux";
import LoginContainer from "../../containers/login/login";
import SignupContainer from "../../containers/signup/signup";
import NavBar from "../navBar/navBar";
import ForgotPassword from "../forgotPassword/forgotPassword";
import ResetPasswordContainer from "../../containers/resetPassword/resetPassword";
import DashboardContainer from "../../containers/dashboard/dashboard";
import MechanicDashboardContainer from "../../containers/mechanicDashboard/mechanicDashboard";
import VerifyVehicleContainer from "../../containers/verifyVehicle/verifyVehicle";
import ContactMechanicContainer from "../../containers/contactMechanic/contactMechanic";
import ChangeEmail from "../changeEmail/changeEmail";
import roleTypes from "../../constants/roleTypes";
import ProfileContainer from "../../containers/profile/profile";
import ShopContainer from "../../containers/shop/shop";
import PastOrdersContainer from "../../containers/pastOrders/pastOrders";
import ForumContainer from "../../containers/forum/forum";
import NewPostContainer from "../../containers/newPost/newPost";
import PostContainer from "../../containers/post/post";

import { logOutUserAction } from "../../actions/userActions";
import { isAccessTokenValid } from "../../utils";

const { Content } = Layout;

/*
 * function to redirect to dashboard if the user is logged in
 * and tries to open login or other pages where log in is not required
 */
const AfterLogin = ({
  component: Component,
  isLoggedIn,
  componentRole,
  userRole,
  accessToken,
  logOutUser,
  ...rest
}) => {
  const hasUserLoggedIn = isLoggedIn;

  return (
    <Route
      {...rest}
      render={(props) => {
        if (hasUserLoggedIn) {
          if (!isAccessTokenValid(accessToken)) {
            logOutUser({
              callback: () => {
                localStorage.clear();
              },
            });
          } else {
            if (!componentRole || (componentRole && componentRole === userRole))
              return <Component {...props} />;
            if (userRole === roleTypes.ROLE_MECHANIC)
              return (
                <Redirect
                  to={{
                    pathname: "/mechanic-dashboard",
                    state: { from: props.location },
                  }}
                />
              );
            return (
              <Redirect
                to={{
                  pathname: "/dashboard",
                  state: { from: props.location },
                }}
              />
            );
          }
        } else
          return (
            <Redirect
              to={{ pathname: "/login", state: { from: props.location } }}
            />
          );
      }}
    />
  );
};

AfterLogin.propTypes = {
  component: PropTypes.any,
  isLoggedIn: PropTypes.bool,
  location: PropTypes.object,
  componentRole: PropTypes.string,
  userRole: PropTypes.string,
  accessToken: PropTypes.string,
  logOutUser: PropTypes.func,
};

/*
 * function to redirect to login if the user is not logged in
 * and tries to open dashboard or other pages where log in is required
 */
const BeforeLogin = ({ component: Component, isLoggedIn, ...rest }) => {
  const hasUserLoggedIn = isLoggedIn;
  return (
    <Route
      {...rest}
      render={(props) =>
        !hasUserLoggedIn ? (
          <Component {...props} />
        ) : (
          <Redirect
            to={{ pathname: "/dashboard", state: { from: props.location } }}
          />
        )
      }
    />
  );
};
BeforeLogin.propTypes = {
  component: PropTypes.any,
  isLoggedIn: PropTypes.bool,
  location: PropTypes.object,
};

const mapStateToProps = ({
  userReducer: { isLoggedIn, role, accessToken, fetchingData },
}) => ({
  isLoggedIn,
  role,
  accessToken,
  fetchingData,
});

const mapDispatchToProps = {
  logOutUser: logOutUserAction,
};

/**
 * function to handle different page rendering based on pathname
 * @param {*} userData
 */
const StyledComp = connect(
  mapStateToProps,
  mapDispatchToProps
)((props) => {
  const [windowHeight, setWindowHeight] = useState(window.innerHeight);

  function handleResize() {
    setWindowHeight(window.innerHeight);
  }

  window.addEventListener("resize", handleResize);

  return (
    <Spin spinning={props.fetchingData} className="spinner">
      <Layout style={{ minHeight: windowHeight }}>
        <Route path="/" component={NavBar} />
        <Content className="layout-content">
          <Switch>
            <BeforeLogin
              path="/login"
              component={LoginContainer}
              isLoggedIn={props.isLoggedIn}
            />
            <BeforeLogin
              path="/signup"
              component={SignupContainer}
              isLoggedIn={props.isLoggedIn}
            />
            <BeforeLogin
              path="/forgot-password"
              component={ForgotPassword}
              isLoggedIn={props.isLoggedIn}
            />
            <AfterLogin
              path="/dashboard"
              component={DashboardContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_USER}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/mechanic-dashboard"
              component={MechanicDashboardContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_MECHANIC}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/reset-password"
              component={ResetPasswordContainer}
              isLoggedIn={props.isLoggedIn}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/my-profile"
              component={ProfileContainer}
              isLoggedIn={props.isLoggedIn}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/change-email"
              component={ChangeEmail}
              isLoggedIn={props.isLoggedIn}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/verify-vehicle"
              component={VerifyVehicleContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_USER}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/contact-mechanic"
              component={ContactMechanicContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_USER}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/shop"
              component={ShopContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_USER}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/past-orders"
              component={PastOrdersContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_USER}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/forum"
              component={ForumContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_USER}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/new-post"
              component={NewPostContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_USER}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <AfterLogin
              path="/post"
              component={PostContainer}
              isLoggedIn={props.isLoggedIn}
              componentRole={roleTypes.ROLE_USER}
              userRole={props.role}
              accessToken={props.accessToken}
              logOutUser={props.logOutUser}
            />
            <Route
              render={() => {
                return (
                  <Redirect
                    to={{
                      pathname: `${
                        !props.isLoggedIn
                          ? "/login"
                          : props.role === roleTypes.ROLE_USER
                          ? "/dashboard"
                          : "/mechanic-dashboard"
                      }`,
                      state: { from: props.location },
                    }}
                  />
                );
              }}
            />
          </Switch>
        </Content>
      </Layout>
    </Spin>
  );
});
StyledComp.propTypes = {
  isLoggedIn: PropTypes.bool,
  role: PropTypes.string,
  logOutUser: PropTypes.func,
  fetchingData: PropTypes.bool,
};

export default StyledComp;
