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

package com.crapi.service.Impl;

import static org.apache.logging.log4j.LogManager.setFactory;

import com.crapi.config.JwtAuthTokenFilter;
import com.crapi.config.JwtProvider;
import com.crapi.constant.UserMessage;
import com.crapi.entity.*;
import com.crapi.enums.EStatus;
import com.crapi.exception.EntityNotFoundException;
import com.crapi.model.*;
import com.crapi.repository.*;
import com.crapi.service.UserService;
import com.crapi.utils.ApiKeyGenerator;
import com.crapi.utils.EmailTokenGenerator;
import com.crapi.utils.MailBody;
import com.crapi.utils.OTPGenerator;
import com.crapi.utils.SMTPMailServer;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.text.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
  static final Log4jContextFactory log4jContextFactory = new Log4jContextFactory();
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private static org.apache.logging.log4j.Logger LOG4J_LOGGER;

  @Autowired ChangeEmailRepository changeEmailRepository;

  @Autowired UserRepository userRepository;

  @Autowired SMTPMailServer smtpMailServer;

  @Autowired ProfileVideoRepository profileVideoRepository;

  @Autowired UserDetailsRepository userDetailsRepository;

  @Autowired PasswordEncoder encoder;

  @Autowired JwtAuthTokenFilter jwtAuthTokenFilter;

  @Autowired JwtProvider jwtProvider;

  @Autowired AuthenticationManager authenticationManager;

  public UserServiceImpl() {
    setFactory(log4jContextFactory);
    LOG4J_LOGGER = LogManager.getLogger(UserService.class);
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
  }

  @Transactional
  @Override
  public ResponseEntity<JwtResponse> authenticateUserLogin(LoginForm loginForm)
      throws IOException, BadCredentialsException {
    JwtResponse jwtResponse = new JwtResponse();
    Authentication authentication = null;
    User user;
    if (loginForm.getEmail() == null) {
      jwtResponse.setMessage(UserMessage.EMAIL_NOT_PROVIDED);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jwtResponse);
    } else {
      if (loginForm.getEmail().contains("jndi:")) {
        if (isLog4jEnabled()) {
          logger.info("Log4j is enabled");
          logger.info(
              "Log4j Exploit Try With Email: {} with Logger: {}, Main Logger: {}",
              loginForm.getEmail(),
              LOG4J_LOGGER.getClass().getName(),
              logger.getClass().getName());
          LOG4J_LOGGER.error("Log4j Exploit Success With Email: {}", loginForm.getEmail());
        } else {
          logger.info("Log4j is disabled");
        }
      }
      user = userRepository.findByEmail(loginForm.getEmail());
      if (user == null) {
        jwtResponse.setMessage(UserMessage.EMAIL_NOT_REGISTERED);
      } else {
        if (user.isMfaRequired()) {
          UserDetails userDetails = userDetailsRepository.findByUser_id(user.getId());
          smtpMailServer.sendMail(
              user.getEmail(), MailBody.mfaMailBody(userDetails), "Unlock your account");
          jwtResponse.setMfaRequired(true);
          jwtResponse.setMessage(UserMessage.OTP_REQUIRED_MESSAGE);
          return ResponseEntity.status(HttpStatus.LOCKED).body(jwtResponse);
        }
        authentication =
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginForm.getEmail(), loginForm.getPassword()));
        String jwt = jwtProvider.generateJwtToken(user);
        if (jwt != null) {
          updateUserToken(jwt, user.getEmail());
          JwtResponse jwtResponse1 = new JwtResponse(jwt);
          jwtResponse1.setMessage(UserMessage.LOGIN_SUCCESSFULL_MESSAGE);
          return ResponseEntity.status(HttpStatus.OK).body(jwtResponse1);
        } else {
          jwtResponse.setMessage(UserMessage.INVALID_CREDENTIALS);
        }
      }
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jwtResponse);
  }

  /**
   * @param token contains JWT token to be verified
   * @return boolean with token valid or not
   */
  @Transactional
  @Override
  public CRAPIResponse verifyJwtToken(String token) {
    boolean validToken = jwtProvider.validateJwtToken(token);
    if (validToken) {
      return new CRAPIResponse(UserMessage.VALID_JWT_TOKEN, 200);
    }

    return new CRAPIResponse(UserMessage.INVALID_JWT_TOKEN, 401);
  }

  /**
   * @param jwt update token in database
   * @param email by email user details and update token
   */
  @Transactional
  @Override
  public void updateUserToken(String jwt, String email) {
    User user = userRepository.findByEmail(email);
    if (user != null) {
      user.setJwtToken(jwt);
      userRepository.saveAndFlush(user);
    }
  }

  /**
   * @param password update password in database
   * @param email by email user details and update password
   */
  @Transactional
  @Override
  public User updateUserPassword(String password, String email) {
    User user = userRepository.findByEmail(email);
    if (user != null) {
      user.setPassword(encoder.encode(password));
      userRepository.saveAndFlush(user);
    }
    return user;
  }

  /**
   * @param loginForm Contains user email, password and number
   * @param request getting jwt token for user from request header
   * @return boolean for user reset password
   */
  @Transactional
  @Override
  public CRAPIResponse resetPassword(LoginForm loginForm, HttpServletRequest request) {
    User user = getUserFromToken(request);
    if (user != null) {
      user.setPassword(encoder.encode(loginForm.getPassword()));
      userRepository.saveAndFlush(user);
      return new CRAPIResponse(UserMessage.PASSWORD_GOT_RESET, 200);
    }
    throw new EntityNotFoundException(User.class, UserMessage.ERROR, loginForm.getEmail());
  }

  /**
   * @param request getting jwt token for user from request header
   * @return user object with profile video and user related data for dashboard api
   */
  @Override
  @Transactional
  public DashboardResponse getUserByRequestToken(HttpServletRequest request) {
    User user;
    UserDetails userDetails;
    DashboardResponse dashboardResponse;
    ProfileVideo profileVideo;
    try {
      // Invalid Signature vulnerability
      // Not Checking the validity of the token for this request
      user = getUserFromTokenWithoutValidation(request);
      userDetails = userDetailsRepository.findByUser_id(user.getId());
      profileVideo = profileVideoRepository.findByUser_id(user.getId());
      dashboardResponse =
          new DashboardResponse(
              user.getId(),
              (userDetails != null ? userDetails.getName() : ""),
              user.getEmail(),
              user.getNumber(),
              user.getRole().toString(),
              userDetails != null ? userDetails.getAvailable_credit() : 0.0);
      if (userDetails != null && userDetails.getPicture() != null)
        dashboardResponse.setPicture_url(userDetails.getPhotoBase64());
      if (profileVideo != null && profileVideo.getVideo() != null) {
        dashboardResponse.setVideo_name(profileVideo.getVideo_name());
        dashboardResponse.setVideo_url(profileVideo.getVideoBase64());
        dashboardResponse.setVideo_id(profileVideo.getId());
      }
      return dashboardResponse;
    } catch (Exception exception) {
      logger.error("fail to load user by email:  -> Message: %d", exception);
      return null;
    }
  }

  /**
   * @param changeEmailForm contains old email and new email, api will send change email token to
   *     new email address.
   * @return send email to new email with random generated token.
   */
  @Transactional
  @Override
  public CRAPIResponse changeEmailRequest(
      HttpServletRequest request, ChangeEmailForm changeEmailForm) {
    String token;
    User user;
    ChangeEmailRequest changeEmailRequest;
    // Checking new email in user login table if it is already registered then not allowing that
    // email
    if (userRepository.existsByEmail(changeEmailForm.getNew_email())) {
      return new CRAPIResponse(
          UserMessage.EMAIL_ALREADY_REGISTERED + changeEmailForm.getNew_email(), 403);
    }
    // Checking old email either it's registered or not.
    if (!userRepository.existsByEmail(changeEmailForm.getOld_email())) {
      return new CRAPIResponse(
          UserMessage.EMAIL_NOT_REGISTERED + changeEmailForm.getOld_email(), 404);
    }
    token = EmailTokenGenerator.generateRandom(10);
    user = getUserFromToken(request);
    // fetching ChangeEmail Data for user
    changeEmailRequest = changeEmailRepository.findByUser(user);
    if (changeEmailRequest == null) {
      // Creating new object if changeEmail data for user is not in database
      changeEmailRequest =
          new ChangeEmailRequest(
              changeEmailForm.getNew_email(), changeEmailForm.getOld_email(), token, user);
    } else {
      // updating the existing changeEmail data for user
      changeEmailRequest.setEmailToken(token);
      changeEmailRequest.setNewEmail(changeEmailForm.getNew_email());
      changeEmailRequest.setOldEmail(changeEmailForm.getOld_email());
    }
    changeEmailForm.setToken(token);
    changeEmailRepository.save(changeEmailRequest);
    smtpMailServer.sendMail(
        changeEmailForm.getNew_email(),
        MailBody.changeMailBody(changeEmailForm),
        "crAPI: Change Email Token");
    return new CRAPIResponse(
        UserMessage.CHANGE_EMAIL_MESSAGE + changeEmailForm.getNew_email(), 200);
  }

  /**
   * @param request getting jwt token for user from request header
   * @param changeEmailForm contains old email and new email, with token, this function will verify
   *     email and token
   * @return its check user token and verify with email token if user verify then correct then we
   *     will update email for user.
   */
  @Transactional
  @Override
  public CRAPIResponse verifyEmailToken(
      HttpServletRequest request, ChangeEmailForm changeEmailForm) {
    ChangeEmailRequest changeEmailRequest;
    User user;
    changeEmailRequest = changeEmailRepository.findByEmailToken(changeEmailForm.getToken());
    user = getUserFromToken(request);
    if (changeEmailRequest != null) {
      if (user.getEmail().equalsIgnoreCase(changeEmailRequest.getOldEmail())) {
        if (changeEmailRequest.getNewEmail().equalsIgnoreCase(changeEmailForm.getNew_email())) {
          user.setEmail(changeEmailRequest.getNewEmail());
          user.setJwtToken("");
          userRepository.save(user);
          return new CRAPIResponse(UserMessage.EMAIL_CHANGE_SUCCESSFUL, 200);
        } else {
          return new CRAPIResponse(UserMessage.NEW_MAIL_DOES_NOT_BELONG, 500);
        }
      } else {
        return new CRAPIResponse(UserMessage.OLD_MAIL_DOES_NOT_BELONG, 500);
      }
    }
    return new CRAPIResponse(UserMessage.INVALID_EMAIL_TOKEN, 500);
  }

  /**
   * @param request request getting jwt token for user from request header
   * @return User object from token throw entity not found if user not found.
   */
  @Transactional
  @Override
  public User getUserFromToken(HttpServletRequest request) {
    User user = null;
    String username = null;
    try {
      username = jwtAuthTokenFilter.getUserFromToken(request);
      if (username != null && !username.equalsIgnoreCase(EStatus.INVALID.toString())) {
        user = userRepository.findByEmail(username);
      }
      if (user != null) {
        return user;
      } else {
        throw new EntityNotFoundException(User.class, "userEmail", username);
      }
    } catch (ParseException exception) {
      logger.error("fail to get username from token -> Message:%d", exception);
      throw new EntityNotFoundException(User.class, "userEmail", username);
    }
  }

  @Transactional
  @Override
  public User getUserFromTokenWithoutValidation(HttpServletRequest request) {
    User user = null;
    try {
      String jwt = jwtAuthTokenFilter.getToken(request);
      String username = jwtProvider.getUserNameFromJwtToken(jwt);
      if (username != null && !username.equalsIgnoreCase(EStatus.INVALID.toString())) {
        user = userRepository.findByEmail(username);
      }

      if (user != null) {
        return user;
      } else {
        throw new EntityNotFoundException(User.class, "userEmail", username);
      }
    } catch (ParseException exception) {
      logger.error("fail to get username from token -> Message:%d", exception);
      throw new EntityNotFoundException(User.class, "userEmail");
    }
  }

  /**
   * @param loginWithEmailToken contains user email and email change token, which allow user login
   *     with email token
   * @return check user and token and return jwt token for user.
   */
  @Transactional
  @Override
  public CRAPIResponse loginWithEmailToken(LoginWithEmailToken loginWithEmailToken) {
    if (loginWithEmailToken.getEmail() == null)
      return new CRAPIResponse(UserMessage.TOKEN_VERIFICATION_MISSING + "email", 400);
    else if (loginWithEmailToken.getToken() == null)
      return new CRAPIResponse(UserMessage.TOKEN_VERIFICATION_MISSING + "token", 400);
    else return new CRAPIResponse(UserMessage.TOKEN_VERIFICATION_FAILOLD, 403);
  }

  /**
   * @param loginWithEmailToken contains user email and email change token, which allow user login
   *     with email token
   * @return check user and token and return jwt token for user.
   */
  @Transactional
  @Override
  public JwtResponse loginWithEmailTokenV2(LoginWithEmailToken loginWithEmailToken) {
    ChangeEmailRequest changeEmailRequest;
    User user;
    String jwt;
    changeEmailRequest = changeEmailRepository.findByEmailToken(loginWithEmailToken.getToken());
    user = userRepository.findByEmail(loginWithEmailToken.getEmail());
    if (changeEmailRequest != null
        && user != null
        && changeEmailRequest.getOldEmail().equalsIgnoreCase(user.getEmail())) {
      jwt = jwtProvider.generateJwtToken(user);
      if (jwt != null) {
        return new JwtResponse(jwt);
      }
    }
    JwtResponse jwtResponse = new JwtResponse();
    jwtResponse.setMessage(UserMessage.INVALID_CREDENTIALS);
    return jwtResponse;
  }

  public boolean isLog4jEnabled() {
    return String.valueOf(System.getenv("ENABLE_LOG4J")).equals("true");
  }

  /**
   * @param lockAccountForm contains user email , which allow admin to lock the user account
   * @return success or failure of account lock
   */
  @Transactional
  @Override
  public CRAPIResponse lockAccount(HttpServletRequest request, LockAccountForm lockAccountForm) {

    String email = lockAccountForm.getEmail();
    try {
      User user = userRepository.findByEmail(email);
      if (user != null) {
        user.setCode(OTPGenerator.generateRandom(8));
        userRepository.save(user);
        return new CRAPIResponse(UserMessage.ACCOUNT_LOCK_MESSAGE, HttpStatus.OK.value());
      } else {
        return new CRAPIResponse(UserMessage.EMAIL_NOT_REGISTERED, HttpStatus.BAD_REQUEST.value());
      }
    } catch (Exception exception) {
      logger.error("fail to lock account  -> Message:%s", exception.getMessage());
    }
    return new CRAPIResponse(UserMessage.ACCOUNT_LOCK_FAILURE, HttpStatus.BAD_REQUEST.value());
  }

  /**
   * @param unlockAccountForm contains user email and password, which allow user to unlock account
   * @return success or failure of account unlock
   */
  @Transactional
  @Override
  public JwtResponse unlockAccount(
      HttpServletRequest request, UnlockAccountForm unlockAccountForm) {
    try {
      String email = unlockAccountForm.getEmail();
      User user = userRepository.findByEmail(email);
      if (user != null) {
        if (unlockAccountForm.getCode().equals(user.getCode())) {
          String jwt = jwtProvider.generateJwtToken(user);
          user.setCode("");
          user.setJwtToken(jwt);
          userRepository.save(user);
          JwtResponse jwtResponse = new JwtResponse(jwt);
          jwtResponse.setMessage(UserMessage.ACCOUNT_UNLOCKED_MESSAGE);
          return jwtResponse;
        }
      }
    } catch (Exception exception) {
      logger.error("fail to unlock account  -> Message:%s", exception.getMessage());
    }
    JwtResponse jwtResponse = new JwtResponse();
    jwtResponse.setMessage(UserMessage.INVALID_CREDENTIALS);
    return jwtResponse;
  }

  @Override
  @Transactional
  public ApiKeyResponse generateApiKey(HttpServletRequest request) {
    User user = getUserFromToken(request);
    if (user != null) {
      String apiKey = ApiKeyGenerator.generateRandom(512);
      logger.debug("Api Key for user {}: {}", user.getEmail(), apiKey);
      user.setApiKey(apiKey);
      userRepository.save(user);
      return new ApiKeyResponse(user.getApiKey(), UserMessage.API_KEY_GENERATED_MESSAGE);
    }
    return new ApiKeyResponse("");
  }
}
