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

package com.crapi.service;

import com.crapi.entity.UserDetails;
import com.crapi.model.*;
import com.crapi.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * @author Traceable AI
 */
public interface UserService {

    CRAPIResponse registerUser(SignUpForm signUpRequest);

    void updateUserToken(String jwt, String email);

    CRAPIResponse resetPassword(LoginForm loginForm, HttpServletRequest request) throws UnsupportedEncodingException;

    DashboardResponse getUserByRequestToken(HttpServletRequest request);

    CRAPIResponse changeEmailRequest(HttpServletRequest request,ChangeEmailForm loginForm);

    CRAPIResponse verifyEmailToken(HttpServletRequest request,ChangeEmailForm changeEmailForm);

    User getUserFromToken(HttpServletRequest request);

    CRAPIResponse loginWithEmailToken(LoginWithEmailToken loginWithEmailToken);

    JwtResponse loginWithEmailTokenV2(LoginWithEmailToken loginWithEmailToken);

    JwtResponse authenticateUserLogin(LoginForm loginForm) throws UnsupportedEncodingException;

    public UserDetails createUserDetails(String name, User user);
}
