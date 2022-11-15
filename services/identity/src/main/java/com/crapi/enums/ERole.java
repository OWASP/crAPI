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

package com.crapi.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ERole {
  ROLE_PREDEFINE("predefined", "Predefined role"),
  ROLE_USER("user", "User role"),
  ROLE_MECHANIC("mechanic", "Mechanic role"),
  ROLE_ADMIN("admin", "Admin role");

  private static final Map<String, ERole> BY_ROLE_NAME = new HashMap<>();
  private final String name;
  private final String description;

  private ERole(String name, String description) {
    this.name = name;
    this.description = description;
  }

  static {
    for (ERole e : values()) {
      BY_ROLE_NAME.put(e.name, e);
    }
  }

  public static Optional<ERole> valueOfName(String name) {
    if (BY_ROLE_NAME.containsKey(name)) {
      return Optional.of(BY_ROLE_NAME.get(name));
    }
    return Optional.empty();
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
