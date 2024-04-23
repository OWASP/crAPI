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

import { APIService } from "../../constants/APIConstant";
const superagent = require("superagent");

class ActionProvider {
  constructor(createChatBotMessage, setStateFunc, createClientMessage) {
    this.createChatBotMessage = createChatBotMessage;
    this.setState = setStateFunc;
    this.createClientMessage = createClientMessage;
  }
  handleNotInitialized = () => {
    const message = this.createChatBotMessage(
      "To initialize the chatbot, please type init and press enter.",
      {
        loading: true,
        terminateLoading: true,
      },
    );
    this.addMessageToState(message);
  };

  handleInitialize = (initRequired) => {
    console.log("Initialization required:", initRequired);
    if (initRequired) {
      this.addOpenApiKeyToState(null);
      this.addInitializingToState();
      const message = this.createChatBotMessage(
        "Please type your OpenAI API key and press enter.",
        {
          loading: true,
          terminateLoading: true,
        },
      );
      this.addMessageToState(message);
    } else {
      const message = this.createChatBotMessage("Bot already initialized", {
        loading: true,
        terminateLoading: true,
      });
      this.addMessageToState(message);
    }
  };

  handleInitialized = (api_key) => {
    if (!api_key) {
      const message = this.createChatBotMessage(
        "Please enter a valid OpenAI API key.",
        {
          loading: true,
          terminateLoading: true,
        },
      );
      this.addMessageToState(message);
      return;
    }
    localStorage.setItem("openapi_key", api_key);
    this.addOpenApiKeyToState(api_key);
    const initUrl = APIService.CHATBOT_SERVICE + "genai/init";
    superagent
      .post(initUrl)
      .send({ openai_api_key: api_key })
      .set("Accept", "application/json")
      .set("Content-Type", "application/json")
      .end((err, res) => {
        if (err) {
          console.log(err);
          const errormessage = this.createChatBotMessage(
            "Failed to initialize chatbot. Please reverify the OpenAI API key.",
            {
              loading: true,
              terminateLoading: true,
            },
          );
          this.addMessageToState(errormessage);
          return;
        }
        console.log(res);
        const successmessage = this.createChatBotMessage(
          "Chatbot initialized successfully.",
          {
            loading: true,
            terminateLoading: true,
          },
        );
        this.addMessageToState(successmessage);
        this.addNotInitializingToState();
      });
    return;
  };

  handleChat = (message) => {
    const chatUrl = APIService.CHATBOT_SERVICE + "genai/ask";
    superagent
      .post(chatUrl)
      .send({ question: message })
      .set("Accept", "application/json")
      .set("Content-Type", "application/json")
      .end((err, res) => {
        if (err) {
          console.log(err);
          const errormessage = this.createChatBotMessage(
            "Failed to get response from chatbot. Please reverify the OpenAI API key.",
            {
              loading: true,
              terminateLoading: true,
            },
          );
          this.addMessageToState(errormessage);
          return;
        }
        console.log(res);
        const successmessage = this.createChatBotMessage(res.body.response, {
          loading: true,
          terminateLoading: true,
        });
        this.addMessageToState(successmessage);
        return;
      });
  };

  handleHelp = (initRequired) => {
    console.log("Initialization required:", initRequired);
    if (initRequired) {
      const message = this.createChatBotMessage(
        "To initialize the chatbot, please type init and press enter. To clear the chat context, type clear or reset and press enter.",
        {
          loading: true,
          terminateLoading: true,
        },
      );
      this.addMessageToState(message);
    } else {
      const message = this.createChatBotMessage(
        "Chat with the bot and exploit it.",
        {
          loading: true,
          terminateLoading: true,
        },
      );
    }
  };

  handleResetContext = () => {
    localStorage.removeItem("chat_messages");
    this.clearMessages();
    const message = this.createChatBotMessage(
      "Chat context has been cleared.",
      {
        loading: true,
        terminateLoading: true,
      },
    );
    this.addMessageToState(message);
  };

  addMessageToState = (message) => {
    this.setState((state) => ({
      ...state,
      messages: [...state.messages, message],
    }));
  };

  addOpenApiKeyToState = (api_key) => {
    this.setState((state) => ({
      ...state,
      openapiKey: api_key,
    }));
  };

  addInitializingToState = () => {
    this.setState((state) => ({
      ...state,
      initializing: true,
    }));
  };

  addNotInitializingToState = () => {
    this.setState((state) => ({
      ...state,
      initializing: false,
    }));
  };

  clearMessages = () => {
    this.setState((state) => ({
      ...state,
      messages: [],
    }));
  };
}

export default ActionProvider;
