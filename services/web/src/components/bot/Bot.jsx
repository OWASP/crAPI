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

import React, { useState, useEffect } from "react";

import config from "./config.jsx";
import { APIService } from "../../constants/APIConstant";
import MessageParser from "./MessageParser.jsx";
import ActionProvider from "./ActionProvider.jsx";
import Chatbot from "react-chatbot-kit";
import { createChatBotMessage } from "react-chatbot-kit";
import {
  PageHeader,
  Card,
  Row,
  Col,
  Tooltip,
  Button,
  Avatar,
  Descriptions,
  Layout,
  Alert,
} from "antd";
import { Space } from "antd";
import Icon, { CloseSquareOutlined, DeleteOutlined } from "@ant-design/icons";
import "./chatbot.css";

const superagent = require("superagent");

const PandaSvg = () => (
  <svg viewBox="0 0 512 512" width="1em" height="1em" fill="currentColor">
    <path
      d="M437.333,21.355H74.667C33.493,21.355,0,54.848,0,96.021v213.333c0,41.173,33.493,74.667,74.667,74.667h48.256    l-36.821,92.032c-1.771,4.395-0.405,9.429,3.328,12.352c1.92,1.515,4.245,2.283,6.592,2.283c2.176,0,4.352-0.661,6.208-1.984    l146.56-104.683h188.587c41.173,0,74.667-33.493,74.667-74.667V96.021C512,54.848,478.507,21.355,437.333,21.355z"
      fill="#1890FF"
    />
  </svg>
);

const PandaIcon = (props) => <Icon component={PandaSvg} {...props} />;

const ChatBotComponent = () => {
  const [chatbotState, setChatbotState] = useState({
    messages: [],
    openapiKey: localStorage.getItem("openapi_key"),
    initializing: false,
    initializationRequired: false,
  });

  const [showBot, toggleBot] = useState(false);

  const saveMessages = (messages, HTMLString) => {
    localStorage.setItem("chat_messages", JSON.stringify(messages));
  };

  const loadMessages = () => {
    const messages = JSON.parse(localStorage.getItem("chat_messages"));
    return messages;
  };

  const clearHistory = () => {
    localStorage.removeItem("chat_messages");
  };

  return (
    <Row>
      <Col xs={10}>
        <div className="app-chatbot-container">
          <div style={{ maxWidth: "500px" }}>
            {showBot && (
              <Chatbot
                config={config}
                botAvator={
                  <Icon
                    icon={PandaIcon}
                    className="app-chatbot-button-icon"
                    style={{ fontSize: "40", color: "white" }}
                  />
                }
                actionProvider={ActionProvider}
                messageParser={MessageParser}
                saveMessages={saveMessages}
                messageHistory={loadMessages()}
                headerText={
                  <Space>
                    Exploit CrapBot &nbsp; &nbsp;
                    <a
                      style={{
                        color: "white",
                        fontWeight: "bold",
                        background: "#0a5e9c",
                        borderRadius: "0px",
                      }}
                      href="#"
                      onClick={() => clearHistory()}
                    >
                      <DeleteOutlined />
                    </a>
                    &nbsp; &nbsp;
                    <a
                      style={{
                        color: "white",
                        fontWeight: "bold",
                        background: "#0a5e9c",
                        borderRadius: "0px",
                      }}
                      href="#"
                      onClick={() => toggleBot((prev) => !prev)}
                    >
                      <CloseSquareOutlined />
                    </a>
                  </Space>
                }
                placeholderText={"Type something..."}
                close="true"
              />
            )}
            <button
              className="app-chatbot-button"
              onClick={() => toggleBot((prev) => !prev)}
            >
              <PandaIcon style={{ fontSize: "24px" }} />
            </button>
          </div>
        </div>
      </Col>
    </Row>
  );
};

export default ChatBotComponent;
