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

import static org.mockito.Mockito.when;

import com.crapi.config.JwtAuthTokenFilter;
import com.crapi.config.JwtProvider;
import com.crapi.constant.UserMessage;
import com.crapi.entity.ChangeEmailRequest;
import com.crapi.entity.ProfileVideo;
import com.crapi.entity.User;
import com.crapi.entity.UserDetails;
import com.crapi.enums.ERole;
import com.crapi.exception.EntityNotFoundException;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.ChangeEmailForm;
import com.crapi.model.DashboardResponse;
import com.crapi.model.JwtResponse;
import com.crapi.model.LoginForm;
import com.crapi.model.LoginWithEmailToken;
import com.crapi.model.SignUpForm;
import com.crapi.repository.ChangeEmailRepository;
import com.crapi.repository.ProfileVideoRepository;
import com.crapi.repository.UserDetailsRepository;
import com.crapi.repository.UserRepository;
import com.crapi.service.VehicleService;
import com.crapi.utils.SMTPMailServer;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import lombok.SneakyThrows;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceImplTest.class);

  @InjectMocks @Spy private UserServiceImpl userService;
  @Mock private PasswordEncoder encoder;
  @Mock private UserRepository userRepository;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtAuthTokenFilter jwtAuthTokenFilter;
  @Mock private JwtProvider jwtProvider;
  @Mock private UserDetailsRepository userDetailsRepository;
  @Mock private VehicleService vehicleService;
  @Mock private SMTPMailServer smtpMailServer;
  @Mock private ProfileVideoRepository profileVideoRepository;
  @Mock private ChangeEmailRepository changeEmailRepository;
  @Mock Appender appender;
  @Captor ArgumentCaptor<LogEvent> logCaptor;

  @Test
  public void resetPassword() {
    LoginForm loginForm = getDummyLoginForm();
    User user = new User("email@example.com", "9798789212", "Pass", ERole.ROLE_USER);
    CRAPIResponse crapiAPIResponse = new CRAPIResponse();
    crapiAPIResponse.setMessage(UserMessage.PASSWORD_GOT_RESET);
    crapiAPIResponse.setStatus(200);
    Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
    Mockito.when(encoder.encode(Mockito.anyString())).thenReturn("newPassword");
    Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(user);
    Assertions.assertEquals(
        userService.resetPassword(loginForm, getMockHttpRequest()).getStatus(), 200);
  }

  @Test(expected = EntityNotFoundException.class)
  public void resetPasswordThrowsExceptionWhenUserNotFound() {
    LoginForm loginForm = getDummyLoginForm();
    User user = getDummyUser();
    CRAPIResponse crapiAPIResponse = new CRAPIResponse();
    crapiAPIResponse.setMessage(UserMessage.PASSWORD_GOT_RESET);
    crapiAPIResponse.setStatus(200);
    Mockito.doReturn(null).when(userService).getUserFromToken(Mockito.any());
    userService.resetPassword(loginForm, getMockHttpRequest());
  }

  @Test(expected = EntityNotFoundException.class)
  public void testGetUserFromTokenThrowsExceptionWhenUserNotFound() throws ParseException {
    Mockito.when(jwtAuthTokenFilter.getUserFromToken(Mockito.any())).thenReturn(null);
    userService.getUserFromToken(getMockHttpRequest());
  }

  @Test(expected = EntityNotFoundException.class)
  @SneakyThrows
  public void testGetUserFromToken() {
    User user = getDummyUser();
    try {
      Mockito.when(jwtAuthTokenFilter.getUserFromToken(Mockito.any())).thenReturn(user.getEmail());
    } catch (ParseException e) {
      logger.error("ParseException");
    }
    Assertions.assertEquals(userService.getUserFromToken(getMockHttpRequest()), user);
    Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(user);
    Assertions.assertEquals(userService.getUserFromToken(getMockHttpRequest()), user);
  }

  @Test
  public void testAuthenticateUserLogin() throws UnsupportedEncodingException {
    LoginForm loginForm = getDummyLoginForm();
    String sampleJwtToken = "sampleToken";
    User user = getDummyUser();
    Mockito.when(authenticationManager.authenticate(Mockito.any()))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
    Mockito.when(jwtProvider.generateJwtToken(Mockito.any())).thenReturn(sampleJwtToken);
    Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(getDummyUser());
    Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(user);
    ResponseEntity<JwtResponse> jwtResponse = userService.authenticateUserLogin(loginForm);
    Assertions.assertEquals(jwtResponse.getBody().getToken(), sampleJwtToken);
    Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
  }

  @Test
  public void testAuthenticateUserLoginLog4J() throws UnsupportedEncodingException {
    LoginForm loginForm = getDummyLoginFormByEmail("${jndi:ldap://127.0.0.1/a}");
    String sampleJwtToken = "sampleToken";
    User user = getDummyUser();
    when(userService.isLog4jEnabled()).thenReturn(true);
    // Mockito.when(authenticationManager.authenticate(Mockito.any()))
    //    .thenReturn(
    //        new UsernamePasswordAuthenticationToken(loginForm.getEmail(),
    // loginForm.getPassword()));
    Mockito.when(jwtProvider.generateJwtToken(Mockito.any())).thenReturn(sampleJwtToken);
    Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(getDummyUser());
    Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(user);
    ResponseEntity<JwtResponse> jwtResponse = userService.authenticateUserLogin(loginForm);
    Assertions.assertEquals(jwtResponse.getBody().getToken(), sampleJwtToken);
    Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
  }

  @Test
  @SneakyThrows
  public void testAuthenticateUserLoginReturnInvalidCredentialsNoPassword()
      throws UnsupportedEncodingException {

    LoginForm loginForm = getDummyLoginFormWithoutPassword();

    Assertions.assertEquals(
        userService.authenticateUserLogin(loginForm).getBody().getMessage(),
        UserMessage.EMAIL_NOT_REGISTERED);
  }

  @Test
  @SneakyThrows
  public void testAuthenticateUserLoginReturnInvalidCredentialsNoEmail()
      throws UnsupportedEncodingException {

    LoginForm loginForm = getDummyLoginFormByEmail(null);

    Assertions.assertEquals(
        userService.authenticateUserLogin(loginForm).getBody().getMessage(),
        UserMessage.EMAIL_NOT_PROVIDED);
  }

  @Test
  @SneakyThrows
  public void testAuthenticateUserLoginInvalidEmail() throws UnsupportedEncodingException {
    LoginForm loginForm = getDummyLoginForm();
    Assertions.assertEquals(
        userService.authenticateUserLogin(loginForm).getBody().getMessage(),
        UserMessage.EMAIL_NOT_REGISTERED);
  }

  @Test
  @SneakyThrows
  public void testAuthenticateUserLoginReturnNullToken() throws UnsupportedEncodingException {
    User user = getDummyUser();
    LoginForm loginForm = getDummyLoginForm();
    Mockito.when(authenticationManager.authenticate(Mockito.any()))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
    Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
    Mockito.when(jwtProvider.generateJwtToken(Mockito.any())).thenReturn(null);
    Assertions.assertEquals(
        userService.authenticateUserLogin(loginForm).getBody().getMessage(),
        UserMessage.INVALID_CREDENTIALS);
  }

  @Test
  public void testUpdateUserToken() {
    User user = getDummyUser();
    String sampleJwt = "sampleToken";
    Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
    Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(user);
    userService.updateUserToken(sampleJwt, getDummyUser().getEmail());
    Assertions.assertEquals(user.getJwtToken(), sampleJwt);
    Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
    Mockito.verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
  }

  @Test
  public void testUpdateUserPassword() {
    User user = getDummyUser();
    String samplePassword = "samplePassword";
    Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
    Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(user);
    userService.updateUserPassword(samplePassword, getDummyUser().getEmail());
    Assertions.assertEquals(user.getPassword(), encoder.encode(samplePassword));
    Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
    Mockito.verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
  }

  @Test
  public void getUserByRequestTokenRequestSuccessFull() {
    User user = getDummyUser();
    UserDetails userDetails = getDummyUserDetails();
    ProfileVideo profileVideo = getDummyProfileVideo();
    Mockito.doReturn(user).when(userService).getUserFromTokenWithoutValidation(Mockito.any());
    userDetailsRepository.findByUser_id(user.getId());
    Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong())).thenReturn(userDetails);
    Mockito.when(profileVideoRepository.findByUser_id(Mockito.anyLong()))
        .thenReturn(getDummyProfileVideo());
    DashboardResponse dashboardResponse = userService.getUserByRequestToken(getMockHttpRequest());
    Assertions.assertNotNull(dashboardResponse);
    Assertions.assertNotNull(dashboardResponse.getPicture_url());
    Assertions.assertNotNull(dashboardResponse.getVideo_name());
    Assertions.assertEquals(dashboardResponse.getVideo_id(), profileVideo.getId());
  }

  @Test
  public void getUserByRequestTokenRequestSuccessFullWhenUserDetailsNull() {
    User user = getDummyUser();
    ProfileVideo profileVideo = getDummyProfileVideo();
    Mockito.doReturn(user).when(userService).getUserFromTokenWithoutValidation(Mockito.any());
    userDetailsRepository.findByUser_id(user.getId());
    Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong())).thenReturn(null);
    Mockito.when(profileVideoRepository.findByUser_id(Mockito.anyLong()))
        .thenReturn(getDummyProfileVideo());
    DashboardResponse dashboardResponse = userService.getUserByRequestToken(getMockHttpRequest());
    Assertions.assertNotNull(dashboardResponse);
    Assertions.assertNull(dashboardResponse.getPicture_url());
    Assertions.assertNotNull(dashboardResponse.getVideo_url());
    Assertions.assertNotNull(dashboardResponse.getVideo_name());
    Assertions.assertEquals(dashboardResponse.getVideo_id(), profileVideo.getId());
  }

  @Test
  public void getUserByRequestTokenRequestSuccessFullWhenProfileVideoNull() {
    User user = getDummyUser();
    UserDetails userDetails = getDummyUserDetails();
    Mockito.doReturn(user).when(userService).getUserFromTokenWithoutValidation(Mockito.any());
    userDetailsRepository.findByUser_id(user.getId());
    Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong())).thenReturn(userDetails);
    Mockito.when(profileVideoRepository.findByUser_id(Mockito.anyLong())).thenReturn(null);
    DashboardResponse dashboardResponse = userService.getUserByRequestToken(getMockHttpRequest());
    Assertions.assertNotNull(dashboardResponse);
    Assertions.assertNotNull(dashboardResponse.getPicture_url());
    Assertions.assertNull(dashboardResponse.getVideo_url());
    Assertions.assertNull(dashboardResponse.getVideo_name());
  }

  @Test
  public void changeEmailRequestNewEmailAlreadyExists() {
    ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
    String expectedMessage = UserMessage.EMAIL_ALREADY_REGISTERED + changeEmailForm.getNew_email();
    Mockito.when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(true);
    CRAPIResponse crapiAPIResponse =
        userService.changeEmailRequest(getMockHttpRequest(), changeEmailForm);
    Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), crapiAPIResponse.getStatus());
  }

  @Test
  public void changeEmailRequestOldEmailDoesNotExists() {
    ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
    String expectedMessage = UserMessage.EMAIL_NOT_REGISTERED + changeEmailForm.getOld_email();
    Mockito.when(userRepository.existsByEmail(changeEmailForm.getNew_email())).thenReturn(false);
    Mockito.when(userRepository.existsByEmail(changeEmailForm.getOld_email())).thenReturn(false);
    CRAPIResponse crapiAPIResponse =
        userService.changeEmailRequest(getMockHttpRequest(), changeEmailForm);
    Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
    Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), crapiAPIResponse.getStatus());
  }

  @Test
  public void changeEmailRequestSuccess() {
    ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
    User user = getDummyUser();
    String expectedMessage = UserMessage.CHANGE_EMAIL_MESSAGE + changeEmailForm.getNew_email();
    ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
    Mockito.when(userRepository.existsByEmail(changeEmailForm.getNew_email())).thenReturn(false);
    Mockito.when(userRepository.existsByEmail(changeEmailForm.getOld_email())).thenReturn(true);
    Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
    Mockito.doReturn(changeEmailRequest).when(changeEmailRepository).save(Mockito.any());
    Mockito.when(changeEmailRepository.findByUser(user)).thenReturn(changeEmailRequest);
    Mockito.doNothing()
        .when(smtpMailServer)
        .sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

    CRAPIResponse crapiAPIResponse =
        userService.changeEmailRequest(getMockHttpRequest(), changeEmailForm);

    Mockito.verify(smtpMailServer, Mockito.times(1))
        .sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
    Assertions.assertEquals(HttpStatus.OK.value(), crapiAPIResponse.getStatus());
  }

  @Test
  public void changeEmailRequestSuccessWhenChangeEmailRequestNull() {
    ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
    User user = getDummyUser();
    String expectedMessage = UserMessage.CHANGE_EMAIL_MESSAGE + changeEmailForm.getNew_email();
    ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
    Mockito.when(userRepository.existsByEmail(changeEmailForm.getNew_email())).thenReturn(false);
    Mockito.when(userRepository.existsByEmail(changeEmailForm.getOld_email())).thenReturn(true);
    Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
    Mockito.doReturn(changeEmailRequest).when(changeEmailRepository).save(Mockito.any());
    Mockito.when(changeEmailRepository.findByUser(user)).thenReturn(null);
    Mockito.doNothing()
        .when(smtpMailServer)
        .sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

    CRAPIResponse crapiAPIResponse =
        userService.changeEmailRequest(getMockHttpRequest(), changeEmailForm);

    Mockito.verify(smtpMailServer, Mockito.times(1))
        .sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
    Assertions.assertEquals(HttpStatus.OK.value(), crapiAPIResponse.getStatus());
  }

  /* @Test
  public void verifyEmailTokenSuccessFull() {
      ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
      User user = getDummyUser();
      String expectedMessage = UserMessage.EMAIL_CHANGE_SUCCESSFUL;
      ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
      changeEmailForm.setNew_email(changeEmailForm.getNew_email());
      changeEmailForm.setOld_email("user@email.com");
      Mockito.when(changeEmailRepository.findByEmailToken(Mockito.anyString()))
              .thenReturn(changeEmailRequest);
      Mockito.doReturn(user).when(userRepository).save(Mockito.any(User.class));
      Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
      CRAPIResponse crapiAPIResponse = userService.verifyEmailToken(getMockHttpRequest(), changeEmailForm);
      Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
      Assertions.assertEquals(HttpStatus.OK.value(), crapiAPIResponse.getStatus());
  }*/

  @Test
  public void verifyEmailTokenFailWhenChangeEmailRequestIsNull() {
    User user = getDummyUser();
    String expectedMessage = UserMessage.INVALID_EMAIL_TOKEN;
    ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
    changeEmailForm.setNew_email(changeEmailForm.getNew_email());
    Mockito.when(changeEmailRepository.findByEmailToken(Mockito.anyString())).thenReturn(null);
    Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
    CRAPIResponse crapiAPIResponse =
        userService.verifyEmailToken(getMockHttpRequest(), changeEmailForm);
    Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), crapiAPIResponse.getStatus());
  }

  @Test
  public void verifyEmailTokenFailWhenChangeEmailFormEmailNotEqualChangeEmailRequestEmail() {
    ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
    User user = getDummyUser();
    String expectedMessage = UserMessage.OLD_MAIL_DOES_NOT_BELONG;
    ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
    changeEmailForm.setNew_email("dummy" + changeEmailForm.getNew_email());
    Mockito.when(changeEmailRepository.findByEmailToken(Mockito.anyString()))
        .thenReturn(changeEmailRequest);
    Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
    CRAPIResponse crapiAPIResponse =
        userService.verifyEmailToken(getMockHttpRequest(), changeEmailForm);
    Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), crapiAPIResponse.getStatus());
  }

  @Test
  public void verifyEmailTokenFailWhenChangeEmailRequestEmailNotEqualUserEmail() {
    ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
    User user = getDummyUser();
    user.setEmail("notequal@email.com");
    String expectedMessage = UserMessage.OLD_MAIL_DOES_NOT_BELONG;
    ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
    changeEmailForm.setNew_email(changeEmailForm.getNew_email());
    Mockito.when(changeEmailRepository.findByEmailToken(Mockito.anyString()))
        .thenReturn(changeEmailRequest);
    Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
    CRAPIResponse crapiAPIResponse =
        userService.verifyEmailToken(getMockHttpRequest(), changeEmailForm);
    Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), crapiAPIResponse.getStatus());
  }

  @Test
  public void loginWithEmailTokenSuccessFull() {
    ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
    LoginWithEmailToken loginWithEmailToken = getDummyLoginWithEmailToken();
    User user = getDummyUser();
    String generatedJwt = "dummyJwt";
    changeEmailRequest.setOldEmail(user.getEmail());
    Mockito.when(changeEmailRepository.findByEmailToken(loginWithEmailToken.getToken()))
        .thenReturn(changeEmailRequest);
    Mockito.when(userRepository.findByEmail(loginWithEmailToken.getEmail())).thenReturn(user);
    Mockito.doReturn(generatedJwt).when(jwtProvider).generateJwtToken(Mockito.any(User.class));
    Assertions.assertEquals(
        userService.loginWithEmailTokenV2(loginWithEmailToken).getToken(), generatedJwt);
  }

  @Test
  public void loginWithEmailTokenFailWhenChangeEmailRequestNotFound() {
    ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
    LoginWithEmailToken loginWithEmailToken = getDummyLoginWithEmailToken();
    User user = getDummyUser();
    String expectedMessage = UserMessage.INVALID_CREDENTIALS;
    String generatedJwt = "dummyJwt";
    changeEmailRequest.setOldEmail(user.getEmail());
    Mockito.when(changeEmailRepository.findByEmailToken(loginWithEmailToken.getToken()))
        .thenReturn(null);
    Mockito.when(userRepository.findByEmail(loginWithEmailToken.getEmail())).thenReturn(user);
    JwtResponse jwtResponse = userService.loginWithEmailTokenV2(loginWithEmailToken);
    Assertions.assertEquals(null, jwtResponse.getToken());
    Assertions.assertEquals(expectedMessage, jwtResponse.getMessage());
  }

  @Test
  public void loginWithEmailTokenFailsWhenUserNotFound() {
    ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
    LoginWithEmailToken loginWithEmailToken = getDummyLoginWithEmailToken();
    User user = getDummyUser();
    String generatedJwt = "dummyJwt";
    String expectedMessage = UserMessage.INVALID_CREDENTIALS;
    changeEmailRequest.setOldEmail(user.getEmail());
    Mockito.when(changeEmailRepository.findByEmailToken(loginWithEmailToken.getToken()))
        .thenReturn(changeEmailRequest);
    Mockito.when(userRepository.findByEmail(loginWithEmailToken.getEmail())).thenReturn(null);
    JwtResponse jwtResponse = userService.loginWithEmailTokenV2(loginWithEmailToken);
    Assertions.assertEquals(null, jwtResponse.getToken());
    Assertions.assertEquals(expectedMessage, jwtResponse.getMessage());
  }

  @Test
  public void loginWithEmailTokenFailWhenUserEmailNotEqualsChangeEmailRequestOldEmail() {
    ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
    LoginWithEmailToken loginWithEmailToken = getDummyLoginWithEmailToken();
    User user = getDummyUser();
    String generatedJwt = "dummyJwt";
    String expectedMessage = UserMessage.INVALID_CREDENTIALS;
    changeEmailRequest.setOldEmail("non" + user.getEmail());
    Mockito.when(changeEmailRepository.findByEmailToken(loginWithEmailToken.getToken()))
        .thenReturn(changeEmailRequest);
    Mockito.when(userRepository.findByEmail(loginWithEmailToken.getEmail())).thenReturn(user);
    JwtResponse jwtResponse = userService.loginWithEmailTokenV2(loginWithEmailToken);
    Assertions.assertEquals(null, jwtResponse.getToken());
    Assertions.assertEquals(expectedMessage, jwtResponse.getMessage());
  }

  @Test
  public void testJwtTokenVerifyWithValidToken() {
    String dummyJwt = "dummyJwt";
    Mockito.when(jwtProvider.validateJwtToken(Mockito.any())).thenReturn(true);
    CRAPIResponse crapiResponse = userService.verifyJwtToken(dummyJwt);
    Assertions.assertEquals(200, crapiResponse.getStatus());
    Assertions.assertEquals(UserMessage.VALID_JWT_TOKEN, crapiResponse.getMessage());
  }

  @Test
  public void testJwtTokenVerifyWithInvalidToken() {
    String dummyJwt = "dummyJwt";
    CRAPIResponse crapiResponse = userService.verifyJwtToken(dummyJwt);
    Assertions.assertEquals(401, crapiResponse.getStatus());
    Assertions.assertEquals(UserMessage.INVALID_JWT_TOKEN, crapiResponse.getMessage());
  }

  private LoginWithEmailToken getDummyLoginWithEmailToken() {
    LoginWithEmailToken loginWithEmailToken = new LoginWithEmailToken();
    loginWithEmailToken.setEmail("user@email.com");
    loginWithEmailToken.setToken("dummyToken");
    return loginWithEmailToken;
  }

  private ChangeEmailRequest getDummyChangeEmailRequest() {
    ChangeEmailRequest changeEmailRequest = new ChangeEmailRequest();
    changeEmailRequest.setEmailToken("dummy token");
    changeEmailRequest.setNewEmail("new@email.com");
    changeEmailRequest.setOldEmail("old@email.com");
    changeEmailRequest.setStatus("DUMMY");
    changeEmailRequest.setUser(getDummyUser());
    changeEmailRequest.setId(1l);
    return changeEmailRequest;
  }

  private ChangeEmailForm getDummyChangeEmailForm() {
    ChangeEmailForm changeEmailForm = new ChangeEmailForm();
    changeEmailForm.setToken("dummyToken");
    changeEmailForm.setNew_email("new@email.com");
    changeEmailForm.setOld_email("old@email.com");
    return changeEmailForm;
  }

  private ProfileVideo getDummyProfileVideo() {
    ProfileVideo profileVideo = new ProfileVideo();
    profileVideo.setVideo_name("New Video");
    profileVideo.setConversion_params("Dummy Conversion Params");
    profileVideo.setUser(getDummyUser());
    profileVideo.setId(1l);
    profileVideo.setVideo(new byte[] {1, 0, 1});
    return profileVideo;
  }

  private SignUpForm getDummySignUpForm() {
    SignUpForm signUpForm = new SignUpForm(1l, "Name", "email@example.com", "9999999");
    signUpForm.setPassword("myPass");
    return signUpForm;
  }

  private UserDetails getDummyUserDetails() {
    UserDetails userDetails = new UserDetails();
    userDetails.setUser(getDummyUser());
    userDetails.setAvailable_credit(200.89);
    userDetails.setName("User1 Details");
    userDetails.setPicture(new byte[] {0, 1, 0});
    return userDetails;
  }

  private User getDummyUser() {
    User user = new User("email@example.com", "9798789212", "Pass", ERole.ROLE_USER);
    user.setId(1l);
    return user;
  }

  private LoginForm getDummyLoginForm() {
    LoginForm loginForm = new LoginForm();
    loginForm.setPassword("password");
    loginForm.setNumber("9798789212");
    loginForm.setEmail("email@example.com");
    return loginForm;
  }

  private LoginForm getDummyLoginFormByEmail(String email) {
    LoginForm loginForm = new LoginForm();
    loginForm.setPassword("password");
    loginForm.setNumber("9798789212");
    loginForm.setEmail(email);
    return loginForm;
  }

  private LoginForm getDummyLoginFormWithoutPassword() {
    LoginForm loginForm = new LoginForm();
    loginForm.setPassword("password");
    loginForm.setNumber("9798789212");
    loginForm.setEmail("email@example.com");
    return loginForm;
  }

  private MockHttpServletRequest getMockHttpRequest() {
    return new MockHttpServletRequest();
  }
}
