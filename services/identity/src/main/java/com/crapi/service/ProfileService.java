package com.crapi.service;

import com.crapi.entity.ProfileVideo;
import com.crapi.entity.UserDetails;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.VideoForm;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Traceabel AI
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
