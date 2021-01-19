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

import com.crapi.constant.UserMessage;
import com.crapi.entity.ProfileVideo;
import com.crapi.entity.User;
import com.crapi.entity.UserDetails;
import com.crapi.exception.IOExceptionHandler;
import com.crapi.exception.CRAPIExceptionHandler;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.VideoForm;
import com.crapi.repository.ProfileVideoRepository;
import com.crapi.repository.UserDetailsRepository;
import com.crapi.service.ProfileService;
import com.crapi.service.UserService;
import com.crapi.utils.BashCommand;
import com.crapi.utils.ProfileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Map;

/**
 * @author Traceable AI
 */
@Service
public class ProfileServiceImpl implements ProfileService {


    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Autowired
    UserService userService;
    @Autowired
    ProfileVideoRepository profileVideoRepository;
    @Autowired
    UserDetailsRepository userDetailsRepository;


    @Value("${app.block_shell_injections}")
    private boolean block_shell_injections;

    @Value("${app.service.name}")
    private String serviceName;

    /**
     * @param file profile image
     * @param request getting jwt token for user from request header
     * @return upload user image in database as BLOB data
     */
    @Transactional
    @Override
    public UserDetails uploadProfilePicture(MultipartFile file, HttpServletRequest request) {
        User user;
        UserDetails userDetails;
        try {
            user = userService.getUserFromToken(request);
            userDetails = userDetailsRepository.findByUser_id(user.getId());
            if (userDetails!=null && ProfileValidator.validateFile(file) != null) {
                userDetails.setPicture(file.getBytes());
            }else{
                userDetails = new UserDetails();
                userDetails.setAvailable_credit(100.0);
                userDetails.setPicture(file.getBytes());
                userDetails.setUser(user);
            }
            userDetailsRepository.save(userDetails);
            return userDetails;
        }catch (IOException exception){
            logger.error("unable to upload image -> Message: %d", exception);
            throw new IOExceptionHandler(ProfileServiceImpl.class, UserMessage.CUSTOM_IO_EXCEPTION);
        }
    }

    /**
     * @param file profile video
     * @param request getting jwt token for user from request header
     * @return upload user video in database as BLOB data
     *
     */
    @Transactional
    @Override
    public ProfileVideo uploadProfileVideo(MultipartFile file,HttpServletRequest request) {
        ProfileVideo profileVideo;
        String conversionParam = "-v codec h264";
        User user;
        try {
            user = userService.getUserFromToken(request);
            profileVideo = profileVideoRepository.findByUser_id(user.getId());
            if (profileVideo != null) {
                profileVideo.setVideo(file.getBytes());
                profileVideo.setConversion_params(conversionParam);
                profileVideo.setVideo_name(file.getOriginalFilename());
            } else {
                profileVideo = new ProfileVideo(file.getOriginalFilename(), file.getBytes(), user);
            }
            profileVideoRepository.save(profileVideo);
            return profileVideo;
        }catch (IOException exception){
            logger.error("unable to upload video -> Message: {} ", exception);
            throw new IOExceptionHandler(ProfileServiceImpl.class,UserMessage.CUSTOM_IO_EXCEPTION);
        }
    }


    /**
     * @param videoId
     * @param request getting jwt token for user from request header
     * @return get profile video details.
     */
    @Transactional
    @Override
    public ProfileVideo getProfileVideo(Long videoId, HttpServletRequest request) {
        User user;
        ProfileVideo profileVideo;
        user = userService.getUserFromToken(request);
        profileVideo = profileVideoRepository.findByUser_id(user.getId());
        if (profileVideo!=null) {
            if (profileVideo.getId() == videoId) {
                return profileVideo;
            }
        }
        throw new CRAPIExceptionHandler(UserMessage.VIDEO_NOT_FOUND,404);
    }

    /**
     * @param videoForm
     * @param request getting jwt token for user from request header
     * @return its change user profile object and return success message.
     */
    @Transactional
    @Override
    public ProfileVideo updateProfileVideo(VideoForm videoForm, HttpServletRequest request) {
        User user;
        ProfileVideo profileVideo;
        user = userService.getUserFromToken(request);
        profileVideo = profileVideoRepository.findByUser_id(user.getId());
        if (profileVideo!=null) {
            profileVideo.setVideo_name(videoForm.getVideoName());
            profileVideo.setConversion_params((videoForm.getConversion_params()!=null?videoForm.getConversion_params():profileVideo.getConversion_params()));
            profileVideoRepository.save(profileVideo);
            return profileVideo;
        }
        throw new CRAPIExceptionHandler(UserMessage.SORRY_DIDNT_GET_PROFILE+user.getEmail(),404);
    }

    /**
     * @param videoId
     * @param request
     * @return boolean for delete object it perform by admin
     */
    @Transactional
    @Override
    public CRAPIResponse deleteProfileVideo(Long videoId, HttpServletRequest request) {
        Optional<ProfileVideo> optionalProfileVideo;
        ProfileVideo profileVideo;
        optionalProfileVideo = profileVideoRepository.findById(videoId);
        if (optionalProfileVideo.isPresent()) {
            return new CRAPIResponse(UserMessage.THIS_IS_ADMIN_FUNCTION,403);
        }
        throw new CRAPIExceptionHandler(UserMessage.SORRY_DIDNT_GET_PROFILE,404);
    }

    /**
     * @param videoId
     * @param request
     * @return boolean for delete object if perform by admin
     */
    @Transactional
    @Override
    public CRAPIResponse deleteAdminProfileVideo(Long videoId, HttpServletRequest request) {
        Optional<ProfileVideo> optionalProfileVideo;
        ProfileVideo profileVideo;
        optionalProfileVideo = profileVideoRepository.findById(videoId);
        User user = userService.getUserFromToken(request);
        if (optionalProfileVideo.isPresent() ) {
            profileVideo = optionalProfileVideo.get();
            profileVideo.setUser(null);
            profileVideoRepository.delete(profileVideo);
            return new CRAPIResponse(UserMessage.VIDEO_DELETED_SUCCESS_MESSAGE,200);
        }
        throw new CRAPIExceptionHandler(UserMessage.SORRY_DIDNT_GET_PROFILE,404);
    }

    /**
     * @param videoId
     * @return fetching profile object by user id and performing the operation according to conversion param
     */
    @Transactional
    @Override
    public CRAPIResponse convertVideo(Long videoId, HttpServletRequest request) {
        BashCommand bashCommand = new BashCommand();
        ProfileVideo profileVideo;
        String host = request.getHeader(HttpHeaders.HOST);
        String xForwardedHost = request.getHeader("x-forwarded-host");
        String scheme = request.getHeader("x-forwarded-proto");
        if (scheme == null) {
            scheme = request.getScheme();
        }
        logger.debug("Convert video {}, host: {}, xForwardedHost: {}, scheme: {}", 
                                                videoId, host, xForwardedHost, scheme);
        try {
            if (xForwardedHost == null) {
                if (videoId!=null && videoId>0) {
                    Optional<ProfileVideo> optionalProfileVideo = profileVideoRepository.findById(videoId);
                    if (optionalProfileVideo.isPresent() && block_shell_injections) {
                        profileVideo = optionalProfileVideo.get();
                        if (ProfileValidator.checkContains(profileVideo.getConversion_params())) {
                            return new CRAPIResponse(UserMessage.CONVERSION_VIDEO_OK, 200);
                        } else if (profileVideo.getConversion_params().equalsIgnoreCase("-v codec h264")) {
                            return new CRAPIResponse(UserMessage.CONVERT_VIDEO_BASH_COMMAND_TRIGGERED, 200);
                        } else if (!profileVideo.getConversion_params().equalsIgnoreCase("-v codec h264")) {
                            if (ProfileValidator.checkSpecialCharacter(profileVideo.getConversion_params())) {
                               return new CRAPIResponse(UserMessage.CONVERT_VIDEO_INTERNAL_ERROR, 500);
                            }
                            return new CRAPIResponse(UserMessage.YOU_WON_THE_GAME, 200);
                        }
                        return new CRAPIResponse(UserMessage.CONVERT_VIDEO_INTERNAL_ERROR, 500);
                    } else if (optionalProfileVideo.isPresent() && !block_shell_injections && optionalProfileVideo.get().getConversion_params() != null) {
                        profileVideo = optionalProfileVideo.get();
                        return new CRAPIResponse(bashCommand.executeBashCommand(profileVideo.getConversion_params()), 200);
                    }
                    return new CRAPIResponse(UserMessage.CONVERT_VIDEO_INTERNAL_ERROR, 500);
                }
                return new CRAPIResponse(UserMessage.CONVERT_VIDEO_PARAM_IS_MISSING, 400);
            } else {
                return new CRAPIResponse(UserMessage.CONVERT_VIDEO_INTERNAL_USE_ONLY, 403);
            }
        } catch (IOException exception) {
            logger.error("unable to convert video -> Message: %d ", exception);
            throw new IOExceptionHandler(ProfileServiceImpl.class, UserMessage.ERROR);
        }
    }
}
