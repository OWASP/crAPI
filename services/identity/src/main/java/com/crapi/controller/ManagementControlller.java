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

import com.crapi.config.JwtProvider;
import com.crapi.model.*;
import com.crapi.service.OtpService;
import com.crapi.service.UserRegistrationService;
import com.crapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account-management API under {@code /identity/management}.
 *
 * <p>{@code POST /admin/lockUser} locks an account (ADMIN role, enforced in {@code
 * WebSecurityConfig}). {@code POST /user/apikey} exchanges an email/password for a never-expiring
 * API key, a JWT signed by {@code JwtProvider} and sent back as {@code Authorization: ApiKey ...}.
 * The chatbot's MCP server ({@code services/chatbot/src/mcpserver/server.py}) calls this endpoint
 * with the hardcoded admin credentials at startup, so every chatbot tool runs with that key.
 *
 * <p>Delegates to {@code UserService}.
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/identity/management")
public class ManagementControlller {
  @Autowired UserService userService;

  @Autowired UserRegistrationService userRegistrationService;

  @Autowired OtpService otpService;

  @Autowired JwtProvider jwtProvider;

  /**
   * @param lockAccountForm contains code to unlock the account
   * @param request getting jwt token for user from request header
   * @return CRAPIResponse with message
   */
  @PostMapping("/admin/lockUser")
  public ResponseEntity<CRAPIResponse> lockAccount(
      @RequestBody LockAccountForm lockAccountForm, HttpServletRequest request)
      throws UnsupportedEncodingException {
    CRAPIResponse response = userService.lockAccount(request, lockAccountForm);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/user/apikey")
  public ResponseEntity<ApiKeyResponse> generateApiKey(
      @RequestBody LoginForm loginForm, HttpServletRequest request)
      throws UnsupportedEncodingException {
    ApiKeyResponse response = userService.generateApiKey(request, loginForm);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
