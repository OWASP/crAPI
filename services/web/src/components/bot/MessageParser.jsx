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

class MessageParser {
  constructor(actionProvider, state) {
    this.actionProvider = actionProvider;
    this.state = state;
  }

  parse(message) {
    console.log("State", this.state);
    console.log("Message", message);
    const message_l = message.toLowerCase();
    if (message_l === "help") {
      return this.actionProvider.handleHelp();
    } else if (message_l === "init" || message_l === "initialize") {
      return this.actionProvider.handleInitialize();
    } else if (this.state.initializing) {
      return this.actionProvider.handleInitialized(message);
    } else if (!this.state.openapiKey) {
      return this.actionProvider.handleNotInitialized();
    } else if (
      message_l === "clear" ||
      message_l === "reset" ||
      message_l === "restart"
    ) {
      return this.actionProvider.handleResetContext();
    }

    return this.actionProvider.handleChat(message);
  }
}

export default MessageParser;
