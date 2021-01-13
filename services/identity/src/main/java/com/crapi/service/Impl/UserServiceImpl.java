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
import com.crapi.enums.EStatus;
import com.crapi.exception.EntityNotFoundException;
import com.crapi.model.*;
import com.crapi.repository.*;
import com.crapi.service.UserService;
import com.crapi.service.VehicleService;
import com.crapi.utils.EmailTokenGenerator;
import com.crapi.utils.MailBody;
import com.crapi.utils.SMTPMailServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author Traceable AI
 */

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    ChangeEmailRepository changeEmailRepository;

    @Autowired
    UserRepository userRepository;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpiration}")
    private int jwtExpiration;

    @Autowired
    SMTPMailServer smtpMailServer;

    @Autowired
    ProfileVideoRepository profileVideoRepository;


    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtAuthTokenFilter jwtAuthTokenFilter;

    @Autowired
    VehicleService vehicleService;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    AuthenticationManager authenticationManager;


    @Transactional
    @Override
    public JwtResponse authenticateUserLogin(LoginForm loginForm) throws UnsupportedEncodingException {
        JwtResponse jwtResponse = new JwtResponse();
        Authentication authentication = null;
        if (loginForm.getEmail()!=null) {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginForm.getEmail(),
                            loginForm.getPassword()
                    )
            );
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);
        if(jwt!=null){
            updateUserToken(jwt, loginForm.getEmail());
            jwtResponse.setToken(jwt);
        }else {
            jwtResponse.setMessage(UserMessage.INVALID_CREDENTIALS);
        }

        return jwtResponse;
    }

    /**
     * @param signUpRequest contains user email,number,name and password
     * @return boolean if user get saved in Database return true else false
     *
     */
    @Transactional
    @Override
    public CRAPIResponse registerUser(SignUpForm signUpRequest) {
        User user;
        UserDetails userDetails;
        VehicleDetails vehicleDetails;
        //Check Number in database
        if(userRepository.existsByNumber(signUpRequest.getNumber())) {
            return new CRAPIResponse(UserMessage.NUMBER_ALREADY_REGISTERED+signUpRequest.getNumber(),403);
        }
        //check Number in database
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new CRAPIResponse(UserMessage.EMAIL_ALREADY_REGISTERED+signUpRequest.getEmail(),403);
        }
        //Register new user in Database
         user = new User(signUpRequest.getEmail(), signUpRequest.getNumber(),
                    encoder.encode(signUpRequest.getPassword()),ERole.ROLE_USER);
         user = userRepository.save(user);
         if (user != null) {
            logger.info("User registered successful with userId {}",user.getId());
            //Creating User Details for same user
             userDetails = createUserDetails(signUpRequest.getName(),user);
             if (userDetails!= null) {
                 userDetailsRepository.save(userDetails);
                 logger.info("User Details Created successful with userId {}",userDetails.getId());
             }

             //Creating User Vehicle
             vehicleDetails = vehicleService.createVehicle();
             if (vehicleDetails!=null) {
                 smtpMailServer.sendMail(user.getEmail(), MailBody.signupMailBody(vehicleDetails, (userDetails!=null && userDetails.getName()!=null?userDetails.getName():"")), "Welcome to crAPI");
                 return new CRAPIResponse(UserMessage.SIGN_UP_SUCCESS_MESSAGE,200);
             }
            throw new EntityNotFoundException(VehicleDetails.class,UserMessage.ERROR, signUpRequest.getName());
        }
        logger.info("User registration failed {}", signUpRequest.getEmail());
        return new CRAPIResponse(UserMessage.SIGN_UP_FAILED+signUpRequest.getEmail(),400);
    }


    /**
     * @param jwt update token in database
     * @param email by email user details and update token
     */
    @Transactional
    @Override
    public void updateUserToken(String jwt, String email) {
        User user = userRepository.findByEmail(email);
        if (user!=null) {
            user.setJwtToken(jwt);
            userRepository.saveAndFlush(user);
        }

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
                return new CRAPIResponse(UserMessage.PASSWORD_GOT_RESET,200);
            }
        throw new EntityNotFoundException(User.class,UserMessage.ERROR, loginForm.getEmail());
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
            user = getUserFromToken(request);
            userDetails = userDetailsRepository.findByUser_id(user.getId());
            profileVideo = profileVideoRepository.findByUser_id(user.getId());
            dashboardResponse  = new DashboardResponse(user.getId(),(userDetails!=null?userDetails.getName():""),
                    user.getEmail(), user.getNumber(),user.getRole().toString(),
                    userDetails!=null?userDetails.getAvailable_credit():0.0);
            if (userDetails!=null &&userDetails.getPicture()!=null )
                dashboardResponse.setPicture_url(userDetails.getPhotoBase64());
            if (profileVideo!=null && profileVideo.getVideo()!=null) {
                dashboardResponse.setVideo_name(profileVideo.getVideo_name());
                dashboardResponse.setVideo_url(profileVideo.getVideoBase64());
                dashboardResponse.setVideo_id(profileVideo.getId());
            }
            return dashboardResponse;
        }catch (Exception exception){
            logger.error("fail to load user by email:  -> Message: %d", exception);
            return null;
        }

    }

    /**
     * @param changeEmailForm contains old email and new email, api will send change email token to new email address.
     * @return send email to new email with random generated token.
     */
    @Transactional
    @Override
    public CRAPIResponse changeEmailRequest(HttpServletRequest request,ChangeEmailForm changeEmailForm) {
        EmailTokenGenerator emailTokenGenerator = new EmailTokenGenerator();
        String token;
        User user;
        ChangeEmailRequest changeEmailRequest;
        //Checking new email in user login table if it is already registered then not allowing that email
        if (userRepository.existsByEmail(changeEmailForm.getNew_email())){
            return new CRAPIResponse(UserMessage.EMAIL_ALREADY_REGISTERED+changeEmailForm.getNew_email(),403);
        }
        //Checking old email either it's registered or not.
        if (!userRepository.existsByEmail(changeEmailForm.getOld_email())){
            return new CRAPIResponse(UserMessage.EMAIL_NOT_REGISTERED+ changeEmailForm.getOld_email(),404);
        }
        token =  emailTokenGenerator.generateRandom(10);
        user = getUserFromToken(request);
        //fetching ChangeEmail Data for user
        changeEmailRequest = changeEmailRepository.findByUser(user);
        if (changeEmailRequest == null) {
            //Creating new object if changeEmail data for user is not in database
            changeEmailRequest = new ChangeEmailRequest(changeEmailForm.getNew_email(), changeEmailForm.getOld_email(), token, user);
        }else {
            //updating the existing changeEmail data for user
            changeEmailRequest.setEmailToken(token);
            changeEmailRequest.setNewEmail(changeEmailForm.getNew_email());
            changeEmailRequest.setOldEmail(changeEmailForm.getOld_email());
        }
        changeEmailForm.setToken(token);
        changeEmailRepository.save(changeEmailRequest);
        smtpMailServer.sendMail(changeEmailForm.getNew_email(), MailBody.changeMailBody(changeEmailForm), "crAPI: Change Email Token");
        return new CRAPIResponse(UserMessage.CHANGE_EMAIL_MESSAGE+ changeEmailForm.getNew_email(),200);
    }

    /**
     * @param request getting jwt token for user from request header
     * @param changeEmailForm contains old email and new email, with token, this function will verify email and token
     * @return its check user token and verify with email token
     * if user verify then correct then we will update email for user.
     */
    @Transactional
    @Override
    public CRAPIResponse verifyEmailToken(HttpServletRequest request,ChangeEmailForm changeEmailForm){
        ChangeEmailRequest changeEmailRequest;
        User user;
        changeEmailRequest = changeEmailRepository.findByEmailToken(changeEmailForm.getToken());
        user = getUserFromToken(request);
        if (changeEmailRequest != null) {
            if (user.getEmail().equalsIgnoreCase(changeEmailRequest.getOldEmail())) {
                if (changeEmailRequest.getNewEmail().equalsIgnoreCase(changeEmailForm.getNew_email())){
                    user.setEmail(changeEmailRequest.getNewEmail());
                    user.setJwtToken("");
                    userRepository.save(user);
                    return new CRAPIResponse(UserMessage.EMAIL_CHANGE_SUCCESSFUL,200);
                } else {
                    return new CRAPIResponse(UserMessage.NEW_MAIL_DOES_NOT_BELONG,500);
                }
            } else {
                return new CRAPIResponse(UserMessage.OLD_MAIL_DOES_NOT_BELONG,500);
            }
        }
        return new CRAPIResponse(UserMessage.INVALID_EMAIL_TOKEN,500);
    }

    /**
     * @param request request getting jwt token for user from request header
     * @return User object from token
     * throw entity not found if user not found.
     */
    @Transactional
    @Override
    public User getUserFromToken(HttpServletRequest request)  {
        User user=null;
        String username=null;
        try {
             username = jwtAuthTokenFilter.getUserFromToken(request);
            if (username!=null && !username.equalsIgnoreCase(EStatus.INVALID.toString())) {
                user = userRepository.findByEmail(username);
            }
            if (user!=null) {
                return user;
            }
            else {
                throw new EntityNotFoundException(User.class, "userEmail", username);
            }
        }catch (UnsupportedEncodingException exception){
            logger.error("fail to get username from token -> Message:%d", exception);
            throw new EntityNotFoundException(User.class,"userEmail", username);
        }
    }


    /**
     * @param loginWithEmailToken contains user email and email change token, which allow user login with email token
     * @return check user and token and return jwt token for user.
     */
    @Transactional
    @Override
    public CRAPIResponse loginWithEmailToken(LoginWithEmailToken loginWithEmailToken) {
        if(loginWithEmailToken.getEmail()==null)
            return new CRAPIResponse(UserMessage.TOKEN_VERIFICATION_MISSING + "email", 400);
        else if(loginWithEmailToken.getToken()==null)
            return new CRAPIResponse(UserMessage.TOKEN_VERIFICATION_MISSING + "token", 400);
        else
            return new CRAPIResponse(UserMessage.TOKEN_VERIFICATION_FAILOLD, 403);
    }


    /**
     * @param loginWithEmailTokenV2 contains user email and email change token, which allow user login with email token
     * @return check user and token and return jwt token for user.
     */
    @Transactional
    @Override
    public JwtResponse loginWithEmailTokenV2(LoginWithEmailToken loginWithEmailToken){
        ChangeEmailRequest changeEmailRequest;
        User user;
        String jwt;
        changeEmailRequest = changeEmailRepository.findByEmailToken(loginWithEmailToken.getToken());
        user = userRepository.findByEmail(loginWithEmailToken.getEmail());
        if(changeEmailRequest!= null && user!=null && changeEmailRequest.getOldEmail().equalsIgnoreCase(user.getEmail())) {
            jwt = generateJWTToken(user);
            if (jwt!=null){
                return new JwtResponse(jwt);
            }
        }

        return new JwtResponse("",UserMessage.INVALID_CREDENTIALS);

    }


    /**
     * @param name is signup user name which will set into user details
     * @param user Mapping user with user details
     * @return create user with default value and mapped with user.
     */
    public UserDetails createUserDetails(String name, User user){
        UserDetails userDetails;
        try {
            userDetails = new UserDetails();
            userDetails.setName(name);
            userDetails.setUser(user);
            userDetails.setAvailable_credit(100.0);
            userDetails.setStatus(EStatus.ACTIVE.toString());
            return userDetails;
        }catch (Exception exception){
            logger.error("fail to create UserDetails  Message: %d", exception);
        }
        return  null;

    }

    /**
     * @param user Generate token according to user email
     * @return token for given user
     *
     */
    public String generateJWTToken(User user) {
        return Jwts.builder()
                .setSubject((user.getEmail()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

}
