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

import com.crapi.entity.ProfileVideo;
import com.crapi.entity.UserDetails;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.VideoForm;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Traceable AI
 */
public interface ProfileService {

    UserDetails uploadProfilePicture(MultipartFile file, HttpServletRequest request);

    ProfileVideo getProfileVideo(Long videoId, HttpServletRequest request);

    ProfileVideo uploadProfileVideo(MultipartFile file,HttpServletRequest request);

    ProfileVideo updateProfileVideo(VideoForm videoForm, HttpServletRequest request);

    CRAPIResponse deleteProfileVideo(Long videoId, HttpServletRequest request);

    CRAPIResponse deleteAdminProfileVideo(Long videoId, HttpServletRequest request);

    CRAPIResponse convertVideo(Long videoId, HttpServletRequest request);
}
