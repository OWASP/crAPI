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
            "Adam", "adam007@example.com", "9876895423", "adam007!123", ERole.ROLE_PREDEFINE));
    users.add(
        new SeedUser(
            "Pogba", "pogba006@example.com", "9876570006", "pogba006!123", ERole.ROLE_PREDEFINE));
    users.add(
        new SeedUser(
            "Robot", "robot001@example.com", "9876570001", "robot001!123", ERole.ROLE_PREDEFINE));
    users.add(new SeedUser("Test", "test@example.com", "9876540001", "Test!123", ERole.ROLE_USER));
    users.add(
        new SeedUser("Admin", "admin@example.com", "9010203040", "Admin!123", ERole.ROLE_ADMIN));
  }
}
