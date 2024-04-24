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
import request from "superagent";

class MessageParser {
  constructor(actionProvider, state) {
    this.actionProvider = actionProvider;
    this.state = state;
  }

  async initializationRequired() {
    const stateUrl = APIService.CHATBOT_SERVICE + "genai/state";
    let initRequired = false;
    // Wait for the response
    await request
      .get(stateUrl)
      .set("Accept", "application/json")
      .set("Content-Type", "application/json")
      .then((res) => {
        console.log("I response:", res.body);
        if (res.status === 200) {
          if (res.body?.initialized === "true") {
            initRequired = false;
          } else {
            initRequired = true;
          }
        }
      })
      .catch((err) => {
        console.log("Error prefetch: ", err);
      });
    console.log("Initialization required:", initRequired);
    return initRequired;
  }

  async parse(message) {
    console.log("State:", this.state);
    console.log("Message:", message);
    const message_l = message.toLowerCase();
    if (this.state?.initializationRequired === undefined) {
      this.state.initializationRequired = await this.initializationRequired();
      console.log("State check:", this.state);
    }
    if (message_l === "help") {
      this.state.initializationRequired = await this.initializationRequired();
      console.log("State help:", this.state);
      return this.actionProvider.handleHelp(this.state.initializationRequired);
    } else if (message_l === "init" || message_l === "initialize") {
      this.state.initializationRequired = await this.initializationRequired();
      console.log("State init:", this.state);
      return this.actionProvider.handleInitialize(
        this.state.initializationRequired,
        this.state.accessToken,
      );
    } else if (
      message_l === "clear" ||
      message_l === "reset" ||
      message_l === "restart"
    ) {
      return this.actionProvider.handleResetContext();
    } else if (this.state.initializing) {
      return this.actionProvider.handleInitialized(message);
    } else if (this.state.initializationRequired) {
      return this.actionProvider.handleNotInitialized();
    }

    return this.actionProvider.handleChat(message, this.state.accessToken);
  }
}

export default MessageParser;
