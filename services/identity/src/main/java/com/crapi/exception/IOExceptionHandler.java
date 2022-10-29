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

package com.crapi.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.util.StringUtils;

public class IOExceptionHandler extends RuntimeException {

  public IOExceptionHandler(Class clazz, Object... searchParamsMap) {
    super(
        IOExceptionHandler.generateMessage(
            clazz.getSimpleName(), toMap(String.class, String.class, searchParamsMap)));
  }

  private static String generateMessage(String entity, Map<String, String> searchParams) {
    return StringUtils.capitalize(entity) + " Unable to store data in database " + searchParams;
  }

  private static <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType, Object... entries) {
    if (entries.length % 2 == 1) throw new IllegalArgumentException("Invalid entries");
    return IntStream.range(0, entries.length / 2)
        .map(i -> i * 2)
        .collect(
            HashMap::new,
            (m, i) -> m.put(keyType.cast(entries[i]), valueType.cast(entries[i + 1])),
            Map::putAll);
  }
}
