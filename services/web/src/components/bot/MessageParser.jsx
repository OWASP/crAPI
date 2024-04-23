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

class MessageParser {
  constructor(actionProvider, state) {
    this.actionProvider = actionProvider;
    this.state = state;
  }

  initializationRequired() {
    const stateUrl = APIService.CHATBOT_SERVICE + "genai/state";
    superagent
      .get(stateUrl)
      .set("Accept", "application/json")
      .set("Content-Type", "application/json")
      .end((err, res) => {
        if (err) {
          console.log(err);
          console.log("Initialization required");
          return true;
        }
        console.log(res);
        if ((res.status = 200)) {
          console.log("Initialization not required");
          return false;
        }
      });
    console.log("Initialization required");
    return true;
  }

  parse(message) {
    console.log("State:", this.state);
    console.log("Message:", message);
    const message_l = message.toLowerCase();
    if (this.state.initializationRequired === undefined) {
      this.state.initializationRequired = this.initializationRequired();
    }
    if (message_l === "help") {
      this.state.initializationRequired = this.initializationRequired();
      return this.actionProvider.handleHelp(this.state.initializationRequired);
    } else if (message_l === "init" || message_l === "initialize") {
      this.state.initializationRequired = this.initializationRequired();
      return this.actionProvider.handleInitialize(
        this.state.initializationRequired,
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

    return this.actionProvider.handleChat(message);
  }
}

export default MessageParser;
