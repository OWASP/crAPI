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
import com.crapi.constant.TestUsers;
import com.crapi.constant.UserMessage;
import com.crapi.entity.User;
import com.crapi.model.*;
import com.crapi.service.OtpService;
import com.crapi.service.UserRegistrationService;
import com.crapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/identity/api/auth")
public class AuthController {
  @Autowired UserService userService;

  @Autowired UserRegistrationService userRegistrationService;

  @Autowired OtpService otpService;

  @Autowired JwtProvider jwtProvider;

  /**
   * @param loginForm contains user email and password for login
   * @return getting jwt token of user from request header
   * @throws UnsupportedEncodingException throws UnsupportedEncodingException for password
   *     encryption
   */
  @PostMapping("/login")
  public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginForm loginForm)
      throws UnsupportedEncodingException {
    try {
      return userService.authenticateUserLogin(loginForm);
    } catch (BadCredentialsException e) {
      JwtResponse jwtResponse = new JwtResponse();
      jwtResponse.setMessage(UserMessage.INVALID_CREDENTIALS);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jwtResponse);
    }
  }

  /**
   * @param signUpRequest contains user email,number,name and password
   * @return success and failure message after user registration.
   */
  @PostMapping("/signup")
  public ResponseEntity<CRAPIResponse> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
    // Creating user's account
    CRAPIResponse registerUserResponse = userRegistrationService.registerUser(signUpRequest);
    if (registerUserResponse != null && registerUserResponse.getStatus() == 200) {
      return ResponseEntity.status(HttpStatus.OK).body(registerUserResponse);
    } else if (registerUserResponse != null && registerUserResponse.getStatus() == 403) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(registerUserResponse);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerUserResponse);
    }
  }

  /**
   * @param verifyTokenRequest contains jwt token
   * @return success and failure message after token authentication.
   */
  @PostMapping("/verify")
  public ResponseEntity<CRAPIResponse> verifyJwtToken(
      @Valid @RequestBody JwtTokenForm verifyTokenRequest) {
    CRAPIResponse verifyTokenResponse = userService.verifyJwtToken(verifyTokenRequest.getToken());
    if (verifyTokenResponse != null && verifyTokenResponse.getStatus() == 200) {
      return ResponseEntity.status(HttpStatus.OK).body(verifyTokenResponse);
    } else if (verifyTokenResponse != null && verifyTokenResponse.getStatus() == 401) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(verifyTokenResponse);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(verifyTokenResponse);
    }
  }

  @GetMapping("/jwks.json")
  public ResponseEntity<String> verifyJwtToken() {
    return ResponseEntity.status(HttpStatus.OK).body(jwtProvider.getPublicJwkSet());
  }

  /**
   * @param forgetPassword contains user email for which user want to generate otp
   * @return success and failure message after generating otp and sent the otp to the register
   *     email.
   */
  @PostMapping("/forget-password")
  public ResponseEntity<CRAPIResponse> forgetPassword(
      @Valid @RequestBody ForgetPassword forgetPassword) {
    CRAPIResponse forgetPasswordResponse = otpService.generateOtp(forgetPassword);
    if (forgetPasswordResponse != null && forgetPasswordResponse.getStatus() == 200) {
      return ResponseEntity.status(HttpStatus.OK).body(forgetPasswordResponse);
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(forgetPasswordResponse);
  }

  /**
   * @param otpForm contains otp, updated password and user email
   * @return success and failure response its non secure API for attacker. in this attacker can
   *     enter 'n' number of times invalid otp
   */
  @PostMapping("/v2/check-otp")
  public ResponseEntity<CRAPIResponse> checkOtp(@RequestBody OtpForm otpForm) {
    CRAPIResponse validateOtpResponse = otpService.validateOtp(otpForm);
    if (validateOtpResponse != null && validateOtpResponse.getStatus() == 200) {
      return ResponseEntity.status(HttpStatus.OK).body(validateOtpResponse);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(validateOtpResponse);
    }
  }

  /**
   * @param otpForm contains otp, updated password and user email
   * @return success and failure response its secure otp validator in this user can enter 10 times
   *     invalid otp after 10 invalid otp it will invalidate the otp.
   */
  @PostMapping("/v3/check-otp")
  public ResponseEntity<CRAPIResponse> secureCheckOtp(@RequestBody OtpForm otpForm) {
    CRAPIResponse validateOtpResponse = otpService.secureValidateOtp(otpForm);
    if (validateOtpResponse.getStatus() == 200) {
      return ResponseEntity.status(HttpStatus.OK).body(validateOtpResponse);
    } else if (validateOtpResponse.getStatus() == 503) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(validateOtpResponse);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(validateOtpResponse);
  }

  /**
   * @param loginWithEmailToken contains user email and email change token, which allow user login
   *     with email token
   * @return double verification message
   */
  @PostMapping("/v4.0/user/login-with-token")
  public ResponseEntity<CRAPIResponse> loginWithToken(
      @RequestBody LoginWithEmailToken loginWithEmailToken) {
    CRAPIResponse response = userService.loginWithEmailToken(loginWithEmailToken);
    return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
  }

  /**
   * @param loginWithEmailToken contains user email and email change token, which allow user login
   *     with email token
   * @return jwt token for login with token
   */
  @PostMapping("/v2.7/user/login-with-token")
  public ResponseEntity<JwtResponse> loginWithTokenV2(
      @Valid @RequestBody LoginWithEmailToken loginWithEmailToken) {
    JwtResponse jwt = userService.loginWithEmailTokenV2(loginWithEmailToken);
    if (jwt.getToken() != null && jwt.getToken().length() > 5) {
      return ResponseEntity.status(HttpStatus.OK).body(jwt);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jwt);
  }

  /** @return success or failure of password updation. */
  @PostMapping("/reset-test-users")
  public ResponseEntity<?> resetPassword() {
    ArrayList<SeedUser> userDetailList = new TestUsers().getUsers();
    for (SeedUser userDetails : userDetailList) {
      User resetUser =
          userService.updateUserPassword(userDetails.getPassword(), userDetails.getEmail());
      if (resetUser == null)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new CRAPIResponse("Internal Server Error", 500));
    }
    return ResponseEntity.status(HttpStatus.OK)
        .body(new CRAPIResponse("Test Users Password Resetted", 200));
  }

  /**
   * @param unlockAccountForm contains code to unlock the account
   * @param request getting jwt token for user from request header
   * @return unlock account for the user. first verify token, validate code and then unlock
   */
  @PostMapping("/unlock")
  public ResponseEntity<JwtResponse> unlockAccount(
      @RequestBody UnlockAccountForm unlockAccountForm, HttpServletRequest request)
      throws UnsupportedEncodingException {

    JwtResponse jwt = userService.unlockAccount(request, unlockAccountForm);
    if (jwt != null && jwt.getToken() != null) {
      return ResponseEntity.ok().body(jwt);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jwt);
  }
}
