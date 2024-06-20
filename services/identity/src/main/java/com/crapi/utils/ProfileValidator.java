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

import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class ProfileValidator {

  /**
   * @param givenString
   * @return boolean for check special character
   */
  public static boolean checkSpecialCharacter(String givenString) {
    Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
    if (regex.matcher(givenString).find()) return true;
    else return false;
  }

  /**
   * @param givenString
   * @return boolean for check selected special character
   */
  public static boolean checkContains(String givenString) {
    Pattern regex = Pattern.compile("[<>;&|]");
    if (regex.matcher(givenString).find()) return true;
    else return false;
  }

  /**
   * @param file
   * @return return file byte data after checking extension
   */
  public static byte[] validateFile(MultipartFile file) {
    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
    try {
      // Check if the file's name contains invalid characters
      if (fileName.contains("..")) {
        log.info("Sorry! Filename contains invalid path sequence " + fileName);
      }
      return file.getBytes();
    } catch (Exception e) {
      log.error("unable to upload video -> Message: %d ", e);
    }
    return null;
  }
}
