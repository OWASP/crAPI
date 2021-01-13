/*
 * Copyright 2020 Traceable, Inc.
 *
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

import com.crapi.config.JwtAuthTokenFilter;
import com.crapi.config.JwtProvider;
import com.crapi.constant.UserMessage;
import com.crapi.entity.*;
import com.crapi.enums.ERole;
import com.crapi.exception.EntityNotFoundException;
import com.crapi.model.*;
import com.crapi.repository.ChangeEmailRepository;
import com.crapi.repository.ProfileVideoRepository;
import com.crapi.repository.UserDetailsRepository;
import com.crapi.repository.UserRepository;
import com.crapi.service.VehicleService;
import com.crapi.utils.SMTPMailServer;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @InjectMocks
    @Spy
    private UserServiceImpl userService;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtAuthTokenFilter jwtAuthTokenFilter;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private UserDetailsRepository userDetailsRepository;
    @Mock
    private VehicleService vehicleService;
    @Mock
    private SMTPMailServer smtpMailServer;
    @Mock
    private ProfileVideoRepository profileVideoRepository;
    @Mock
    private ChangeEmailRepository changeEmailRepository;

    @Test
    public void resetPassword() {
        LoginForm loginForm = getDummyLoginForm();
        User user = new User("email@example.com", "9798789212", "Pass", ERole.ROLE_USER);
        CRAPIResponse crapiAPIResponse = new CRAPIResponse();
        crapiAPIResponse.setMessage(UserMessage.PASSWORD_GOT_RESET);
        crapiAPIResponse.setStatus(200);
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        Mockito.when(encoder.encode(Mockito.anyString()))
                .thenReturn("newPassword");
        Mockito.when(userRepository.saveAndFlush(Mockito.any()))
                .thenReturn(user);
        Assertions.assertEquals(userService.resetPassword(loginForm, getMockHttpRequest()).getStatus(), 200);
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
    public void testGetUserFromTokenThrowsExceptionWhenUserNotFound() throws UnsupportedEncodingException {
        Mockito.when(jwtAuthTokenFilter.getUserFromToken(Mockito.any()))
                .thenReturn(null);
        userService.getUserFromToken(getMockHttpRequest());
    }

    @Test
    @SneakyThrows
    public void testGetUserFromToken() {
        User user = getDummyUser();
        Mockito.when(jwtAuthTokenFilter.getUserFromToken(Mockito.any()))
                .thenReturn(user.getEmail());
        Mockito.when(userRepository.findByEmail(Mockito.any()))
                .thenReturn(user);
        Assertions.assertEquals(userService.getUserFromToken(getMockHttpRequest()), user);
    }

    @Test
    public void testAuthenticateUserLogin() throws UnsupportedEncodingException {
        LoginForm loginForm = getDummyLoginForm();
        String sampleJwtToken = "sampleToken";
        User user = getDummyUser();
        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        loginForm.getEmail(),
                        loginForm.getPassword()));
        Mockito.when(jwtProvider.generateJwtToken(Mockito.any()))
                .thenReturn(sampleJwtToken);
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(getDummyUser());
        Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(user);
        Assertions.assertEquals(userService.authenticateUserLogin(loginForm).getToken(), sampleJwtToken);
        Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
    }

    @Test
    @SneakyThrows
    public void testAuthenticateUserLoginReturnInvalidCredentials() {
        LoginForm loginForm = getDummyLoginForm();
        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        loginForm.getEmail(),
                        loginForm.getPassword()));
        Mockito.when(jwtProvider.generateJwtToken(Mockito.any()))
                .thenReturn(null);
        Assertions.assertEquals(userService.authenticateUserLogin(loginForm).getMessage(), UserMessage.INVALID_CREDENTIALS);
    }

    @Test
    public void testUpdateUserToken() {
        User user = getDummyUser();
        String sampleJwt = "sampleToken";
        Mockito.when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(user);
        Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(user);
        userService.updateUserToken(sampleJwt, getDummyUser().getEmail());
        Assertions.assertEquals(user.getJwtToken(), sampleJwt);
        Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
    }

    @Test
    public void registerUserReturnsNumberAlreadyExists() {
        SignUpForm signUpForm = getDummySignUpForm();
        String expectedMessage = UserMessage.NUMBER_ALREADY_REGISTERED + signUpForm.getNumber();
        Mockito.when(userRepository.existsByNumber(signUpForm.getNumber()))
                .thenReturn(true);
        Assertions.assertEquals(userService.registerUser(signUpForm).getMessage(), expectedMessage);
        Mockito.verify(userRepository, Mockito.times(1)).existsByNumber(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(0)).existsByEmail(Mockito.any());
    }

    @Test
    public void registerUserReturnsEmailAlreadyExists() {
        SignUpForm signUpForm = getDummySignUpForm();
        String expectedMessage = UserMessage.EMAIL_ALREADY_REGISTERED + signUpForm.getEmail();
        Mockito.when(userRepository.existsByNumber(signUpForm.getNumber()))
                .thenReturn(false);
        Mockito.when(userRepository.existsByEmail(signUpForm.getEmail()))
                .thenReturn(true);
        Assertions.assertEquals(userService.registerUser(signUpForm).getMessage(), expectedMessage);
        Mockito.verify(userRepository, Mockito.times(1)).existsByNumber(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(Mockito.any());
    }


    @Test(expected = EntityNotFoundException.class)
    public void registerUserFailsNotAbleToCreateVehicleDetails() {
        SignUpForm signUpForm = getDummySignUpForm();
        User user = getDummyUser();
        Mockito.when(userRepository.existsByNumber(signUpForm.getNumber()))
                .thenReturn(false);
        Mockito.when(userRepository.existsByEmail(signUpForm.getEmail()))
                .thenReturn(false);
        Mockito.when(encoder.encode(Mockito.anyString()))
                .thenReturn("EncodedString");
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(user);
        Mockito.when(userDetailsRepository.save(Mockito.any(UserDetails.class)))
                .thenReturn(getDummyUserDetails());
        Mockito.when(vehicleService.createVehicle())
                .thenReturn(null);
        userService.registerUser(signUpForm);
    }

    @Test
    public void registerUserSuccessFull() {
        SignUpForm signUpForm = getDummySignUpForm();
        User user = getDummyUser();
        Mockito.when(userRepository.existsByNumber(signUpForm.getNumber()))
                .thenReturn(false);
        Mockito.when(userRepository.existsByEmail(signUpForm.getEmail()))
                .thenReturn(false);
        Mockito.when(encoder.encode(Mockito.anyString()))
                .thenReturn("EncodedString");
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(user);
        Mockito.when(userDetailsRepository.save(Mockito.any(UserDetails.class)))
                .thenReturn(getDummyUserDetails());
        Mockito.when(vehicleService.createVehicle())
                .thenReturn(new VehicleDetails());
        Mockito.doNothing().when(smtpMailServer).sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Assertions.assertEquals(userService.registerUser(signUpForm).getStatus(), HttpStatus.OK.value());
        Mockito.verify(smtpMailServer, Mockito.times(1)).sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void getUserByRequestTokenRequestSuccessFull() {
        User user = getDummyUser();
        UserDetails userDetails = getDummyUserDetails();
        ProfileVideo profileVideo = getDummyProfileVideo();
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        userDetailsRepository.findByUser_id(user.getId());
        Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong()))
                .thenReturn(userDetails);
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
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        userDetailsRepository.findByUser_id(user.getId());
        Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong()))
                .thenReturn(null);
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
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        userDetailsRepository.findByUser_id(user.getId());
        Mockito.when(userDetailsRepository.findByUser_id(Mockito.anyLong()))
                .thenReturn(userDetails);
        Mockito.when(profileVideoRepository.findByUser_id(Mockito.anyLong()))
                .thenReturn(null);
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
        Mockito.when(userRepository.existsByEmail(Mockito.anyString()))
                .thenReturn(true);
        CRAPIResponse crapiAPIResponse = userService
                .changeEmailRequest(getMockHttpRequest(), changeEmailForm);
        Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), crapiAPIResponse.getStatus());
    }


    @Test
    public void changeEmailRequestOldEmailDoesNotExists() {
        ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
        String expectedMessage = UserMessage.EMAIL_NOT_REGISTERED + changeEmailForm.getOld_email();
        Mockito.when(userRepository.existsByEmail(changeEmailForm.getNew_email()))
                .thenReturn(false);
        Mockito.when(userRepository.existsByEmail(changeEmailForm.getOld_email()))
                .thenReturn(false);
        CRAPIResponse crapiAPIResponse = userService
                .changeEmailRequest(getMockHttpRequest(), changeEmailForm);
        Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), crapiAPIResponse.getStatus());
    }

    @Test
    public void changeEmailRequestSuccess() {
        ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
        User user = getDummyUser();
        String expectedMessage = UserMessage.CHANGE_EMAIL_MESSAGE + changeEmailForm.getNew_email();
        ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
        Mockito.when(userRepository.existsByEmail(changeEmailForm.getNew_email()))
                .thenReturn(false);
        Mockito.when(userRepository.existsByEmail(changeEmailForm.getOld_email()))
                .thenReturn(true);
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        Mockito.doReturn(changeEmailRequest).when(changeEmailRepository).save(Mockito.any());
        Mockito.when(changeEmailRepository.findByUser(user))
                .thenReturn(changeEmailRequest);
        Mockito.doNothing().when(smtpMailServer).sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        CRAPIResponse crapiAPIResponse = userService.changeEmailRequest(getMockHttpRequest(), changeEmailForm);

        Mockito.verify(smtpMailServer, Mockito.times(1)).sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.OK.value(), crapiAPIResponse.getStatus());

    }

    @Test
    public void changeEmailRequestSuccessWhenChangeEmailRequestNull() {
        ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
        User user = getDummyUser();
        String expectedMessage = UserMessage.CHANGE_EMAIL_MESSAGE + changeEmailForm.getNew_email();
        ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
        Mockito.when(userRepository.existsByEmail(changeEmailForm.getNew_email()))
                .thenReturn(false);
        Mockito.when(userRepository.existsByEmail(changeEmailForm.getOld_email()))
                .thenReturn(true);
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        Mockito.doReturn(changeEmailRequest).when(changeEmailRepository).save(Mockito.any());
        Mockito.when(changeEmailRepository.findByUser(user))
                .thenReturn(null);
        Mockito.doNothing().when(smtpMailServer).sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        CRAPIResponse crapiAPIResponse = userService.changeEmailRequest(getMockHttpRequest(), changeEmailForm);

        Mockito.verify(smtpMailServer, Mockito.times(1)).sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
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
        Mockito.when(changeEmailRepository.findByEmailToken(Mockito.anyString()))
                .thenReturn(null);
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        CRAPIResponse crapiAPIResponse = userService.verifyEmailToken(getMockHttpRequest(), changeEmailForm);
        Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), crapiAPIResponse.getStatus());
    }

    @Test
    public void verifyEmailTokenFailWhenChangeEmailFormEmailNotEqualChangeEmailRequestEmail() {
        ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
        User user = getDummyUser();
        String expectedMessage = UserMessage.INVALID_EMAIL_TOKEN;
        ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
        changeEmailForm.setNew_email("dummy" + changeEmailForm.getNew_email());
        Mockito.when(changeEmailRepository.findByEmailToken(Mockito.anyString()))
                .thenReturn(changeEmailRequest);
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        CRAPIResponse crapiAPIResponse = userService.verifyEmailToken(getMockHttpRequest(), changeEmailForm);
        Assertions.assertEquals(expectedMessage, crapiAPIResponse.getMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), crapiAPIResponse.getStatus());
    }

    @Test
    public void verifyEmailTokenFailWhenChangeEmailRequestEmailNotEqualUserEmail() {
        ChangeEmailRequest changeEmailRequest = getDummyChangeEmailRequest();
        User user = getDummyUser();
        user.setEmail("notequal@email.com");
        String expectedMessage = UserMessage.INVALID_EMAIL_TOKEN;
        ChangeEmailForm changeEmailForm = getDummyChangeEmailForm();
        changeEmailForm.setNew_email(changeEmailForm.getNew_email());
        Mockito.when(changeEmailRepository.findByEmailToken(Mockito.anyString()))
                .thenReturn(changeEmailRequest);
        Mockito.doReturn(user).when(userService).getUserFromToken(Mockito.any());
        CRAPIResponse crapiAPIResponse = userService.verifyEmailToken(getMockHttpRequest(), changeEmailForm);
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
        Mockito.when(userRepository.findByEmail(loginWithEmailToken.getEmail()))
                .thenReturn(user);
        Mockito.doReturn(generatedJwt).when(userService).generateJWTToken(Mockito.any(User.class));
        Assertions.assertEquals(userService.loginWithEmailTokenV2(loginWithEmailToken).getToken(), generatedJwt);
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
        Mockito.when(userRepository.findByEmail(loginWithEmailToken.getEmail()))
                .thenReturn(user);
        JwtResponse jwtResponse = userService.loginWithEmailTokenV2(loginWithEmailToken);
        Assertions.assertEquals("", jwtResponse.getToken());
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
        Mockito.when(userRepository.findByEmail(loginWithEmailToken.getEmail()))
                .thenReturn(null);
        JwtResponse jwtResponse = userService.loginWithEmailTokenV2(loginWithEmailToken);
        Assertions.assertEquals("", jwtResponse.getToken());
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
        Mockito.when(userRepository.findByEmail(loginWithEmailToken.getEmail()))
                .thenReturn(user);
        JwtResponse jwtResponse = userService.loginWithEmailTokenV2(loginWithEmailToken);
        Assertions.assertEquals("", jwtResponse.getToken());
        Assertions.assertEquals(expectedMessage, jwtResponse.getMessage());
    }

    private LoginWithEmailToken getDummyLoginWithEmailToken(){
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
        profileVideo.setVideo(new byte[]{1, 0, 1});
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
        userDetails.setPicture(new byte[]{0, 1, 0});
        return userDetails;
    }

    private User getDummyUser() {
        User user = new User("email@example.com", "9798789212", "Pass", ERole.ROLE_USER);
        user.setId(1l);
        return user;
    }

    private LoginForm getDummyLoginForm(){
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
