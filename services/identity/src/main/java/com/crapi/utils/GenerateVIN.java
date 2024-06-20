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

import java.util.Random;

public class GenerateVIN {

  static String charsequence = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  static String num = "0123456789";
  String vin = "";
  String pincode = "";
  Random random;

  public GenerateVIN() {
    random = new Random();
  }

  public GenerateVIN(long seed) {
    random = new Random(seed);
  }

  /** @return random generate pin code for add vehicle */
  public String generatePincode() {
    pincode += getNum(3);
    return pincode;
  }

  /** @return rendom generate VIN for vehicle */
  public String generateVIN() {
    vin += getNum(0) + getChar(3) + getNum(1) + getChar(3) + getNum(5);
    return vin;
  }

  public String getChar(int num) {
    String randStr = "";
    for (int j = 0; j <= num; j++) randStr += randomCharacter();
    return randStr;
  }

  public String getNum(int num) {
    String randNum = "";
    for (int k = 0; k <= num; k++) randNum += randomNumber();
    return randNum;
  }

  public String randomCharacter() {
    int n = charsequence.length();
    int r = random.nextInt(n);
    return charsequence.substring(r, r + 1);
  }

  public String randomNumber() {
    int n = num.length();
    int r = random.nextInt(n);
    return num.substring(r, r + 1);
  }
}
