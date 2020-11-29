package com.crapi.service;

import com.crapi.entity.UserDetails;
import com.crapi.model.*;
import com.crapi.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * @author Traceabel AI
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
