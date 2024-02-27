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

import org.springframework.stereotype.Component;

@Component
public class ApiKeyGenerator {

  public static String characters =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  /**
   * @param length
   * @return generate random otp for forgot password
   */
  public static String generateRandom(int length) {
    String apiKey = "";
    for (int i = 0; i < length; i++) {
      apiKey += randomCharacter(characters);
    }
    return apiKey;
  }

  public static String randomCharacter(String characters) {
    int n = characters.length();
    int r = (int) (n * Math.random());
    return characters.substring(r, r + 1);
  }
}
