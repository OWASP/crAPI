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

package com.crapi.constant;

import com.crapi.enums.ERole;
import com.crapi.model.SeedUser;
import java.util.ArrayList;
import lombok.Getter;

public class TestUsers {
  @Getter public ArrayList<SeedUser> users = new ArrayList<SeedUser>();

  public TestUsers() {
    users.add(
        new SeedUser(
            "Adam",
            "adam007@example.com",
            "9876895423",
            "adam007!123",
            ERole.ROLE_PREDEFINE,
            "f89b5f21-7829-45cb-a650-299a61090378",
            "7ECOX34KJTV359804",
            "123456",
            "32.778889",
            "-91.919243"));
    users.add(
        new SeedUser(
            "Pogba",
            "pogba006@example.com",
            "9876570006",
            "pogba006!123",
            ERole.ROLE_PREDEFINE,
            "cd515c12-0fc1-48ae-8b61-9230b70a845b",
            "8VAUI03PRUQ686911",
            "123456",
            "31.284788",
            "-92.471176"));
    users.add(
        new SeedUser(
            "Robot",
            "robot001@example.com",
            "9876570001",
            "robot001!123",
            ERole.ROLE_PREDEFINE,
            "4bae9968-ec7f-4de3-a3a0-ba1b2ab5e5e5",
            "0NKPZ09IHOP508673",
            "123456",
            "37.746880",
            "-84.301460"));
    users.add(
        new SeedUser(
            "Test",
            "test@example.com",
            "9876540001",
            "Test!123",
            ERole.ROLE_USER,
            "1929186d-8b67-4163-a208-de52a41f7301",
            "8IGEF39BZUJ159285",
            "123456",
            "38.206348",
            "-84.270172"));
    users.add(
        new SeedUser(
            "Admin",
            "admin@example.com",
            "9010203040",
            "Admin!123",
            ERole.ROLE_ADMIN,
            "f5c506f5-3af2-4120-926c-64ad8b10ddc2",
            "6NBBY70FWUM324316",
            "123456",
            "37.406769",
            "-94.705528"));
  }
}
