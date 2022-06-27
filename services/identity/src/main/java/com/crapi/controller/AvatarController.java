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

package com.crapi.controller;

import com.crapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@CrossOrigin
@RestController
@RequestMapping("/identity/api/v2/avatar")
public class AvatarController {



    @Autowired
    UserService userService;

    /**
     * @param user_id
     * @param request
     * @return the specified user's avatar
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<?> avatar(@PathVariable("user_id") Long user_id, HttpServletRequest request) {
      byte[] avatarData = userService.getUserAvatar(user_id, request);
      if (avatarData != null) {
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.IMAGE_JPEG).body(avatarData);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
    }

}
