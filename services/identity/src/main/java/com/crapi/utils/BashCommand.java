/*
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

package com.crapi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BashCommand {

  /**
   * Execute a bash command. We can handle complex bash commands including multiple executions (; |
   * && ||), quotes, expansions ($), escapes (\), e.g.: "cd /abc/def; mv ghi 'older ghi '$(whoami)"
   *
   * @param command
   * @return true if bash got started, but your command may have failed.
   */
  public String executeBashCommand(String command) throws IOException {
    BufferedReader b = null;
    StringBuilder output;
    log.info("Executing BASH command:\n   ", command);
    Runtime r = Runtime.getRuntime();
    // Use bash -c so we can handle things like multi commands separated by ; and
    // things like quotes, $, |, and \. My tests show that command comes as
    // one argument to bash, so we do not need to quote it to make it one thing.
    // Also, exec may object if it does not have an executable file as the first thing,
    // so having bash here makes it happy provided bash is installed and in path.
    String[] commands = {"bash", "-c", command};
    try {
      Process p = r.exec(commands);

      p.waitFor();
      InputStreamReader data = new InputStreamReader(p.getInputStream());
      b = new BufferedReader(data);
      String line = "";
      output = new StringBuilder();
      while ((line = b.readLine()) != null) {
        output.append(line + "\n");
      }
      b.close();
      return (output != null ? String.valueOf(output) : "command not found");
    } catch (Exception e) {
      log.error("Failed to execute bash with command: " + command);
      e.printStackTrace();
    } finally {
      b.close();
    }
    return null;
  }
}
