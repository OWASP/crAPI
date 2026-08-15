/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import "./nav.css";

import { Button, Dropdown, Menu, Avatar, Layout, Space } from "antd";
import {
  LogoutOutlined,
  EditOutlined,
  ProfileOutlined,
  CaretDownOutlined,
} from "@ant-design/icons";
import React from "react";
import roleTypes from "../../constants/roleTypes";
import type { MenuProps } from "antd";
import { useNavigate } from "react-router-dom";
import { connect, ConnectedProps } from "react-redux";
import { logOutUserAction } from "../../actions/userActions";
import defaultProficPic from "../../assets/default_profile_pic.png";

const { Header } = Layout;

interface RootState {
  userReducer: {
    accessToken: string;
    name: string;
    isLoggedIn: boolean;
    role: string;
  };
  profileReducer: {
    profilePicData: string;
  };
}

interface NavbarProps extends PropsFromRedux {}

/**
 * top navigation bar that contains
 * if not logged in:
 * Login button,
 * ShopEase icon,
 * if logged in :
 * dropdown to navigate to change Password or My Profile
 * dropdown alos consists the logout button
 */
const Navbar: React.FC<NavbarProps> = (props) => {
  const { logOutUser, isLoggedIn, name, role, profilePicData } = props;
  const navigate = useNavigate();

  const logout = () => {
    logOutUser({
      callback: () => {
        localStorage.clear();
      },
    });
  };

  const takeNavigationAction = (input: { key: string }) => {
    if (input.key === "dashboard") navigate(`/`);
    else if (input.key === "shop") navigate(`/shop`);
    else if (input.key === "forum") navigate(`/forum`);
  };

  type MenuItem = Required<MenuProps>["items"][number];

  const menuitems: MenuItem[] = [];
  menuitems.push({ key: "dashboard", label: "Dashboard" });
  if (role !== roleTypes.ROLE_MECHANIC) {
    menuitems.push({ key: "shop", label: "Shop" });
    menuitems.push({ key: "forum", label: "Community" });
  }

  const menuNavigation = () => (
    <Menu
      onClick={(key) => takeNavigationAction(key)}
      mode="horizontal"
      theme="dark"
      items={menuitems}
    />
  );

  const sideMenuItems: MenuProps["items"] = [
    {
      key: "profile",
      label: "My Profile",
      onClick: () => navigate("/my-profile"),
      icon: <ProfileOutlined />,
    },
    {
      key: "password",
      label: "Change Password",
      onClick: () => navigate("/reset-password"),
      icon: <EditOutlined />,
    },
    {
      key: "logout",
      label: "Logout",
      onClick: logout,
      icon: <LogoutOutlined />,
    },
  ];

  return (
    <Header>
      <Space className="top-nav-left">
        <div className="logo-text" onClick={() => navigate("/")}>
          ShopEase
        </div>
        {isLoggedIn ? menuNavigation() : <div />}
      </Space>
      {isLoggedIn ? (
        <Space className="top-nav-right">
          <div className="greeting-text">{`Good Morning, ${name}!`}</div>
          <Dropdown
            menu={{ items: sideMenuItems }}
            placement="bottomRight"
            trigger={["click", "hover"]}
          >
            <div className="profile-dropdown-trigger">
              <div className="avatarContainer">
                <Avatar
                  src={profilePicData || defaultProficPic}
                  className="avatar"
                  size="large"
                />
              </div>
              <div className="dropdown-info">
                <span className="user-name">{name}</span>
                <CaretDownOutlined className="dropdown-arrow" />
              </div>
            </div>
          </Dropdown>
        </Space>
      ) : (
        <>
          <Space className="top-nav-right">
            <Button
              className="navbar-button"
              onClick={() => {
                navigate("/login");
              }}
            >
              Login
            </Button>
            <Button
              className="navbar-button"
              onClick={() => navigate("/signup")}
            >
              Signup
            </Button>
          </Space>
        </>
      )}
    </Header>
  );
};

const mapStateToProps = (state: RootState) => ({
  accessToken: state.userReducer.accessToken,
  name: state.userReducer.name,
  isLoggedIn: state.userReducer.isLoggedIn,
  profilePicData: state.profileReducer.profilePicData,
  role: state.userReducer.role,
});

const mapDispatchToProps = {
  logOutUser: logOutUserAction,
};

const connector = connect(mapStateToProps, mapDispatchToProps);

type PropsFromRedux = ConnectedProps<typeof connector>;

export default connector(Navbar);
