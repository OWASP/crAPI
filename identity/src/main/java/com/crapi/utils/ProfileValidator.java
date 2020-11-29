package com.crapi.utils;

import com.crapi.service.Impl.ProfileServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;


/**
 * @author Traceabel AI
 */
public class ProfileValidator {

    private static final Logger logger = LoggerFactory.getLogger(ProfileValidator.class);

    /**
     * @param givenString
     * @return boolean for check special character
     */
    public static boolean checkSpecialCharacter(String givenString){
        Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
        if (regex.matcher(givenString).find())
            return true;
        else
            return false;
    }

    /**
     * @param givenString
     * @return boolean for check selected special character
     */
    public static boolean checkContains(String givenString){
        Pattern regex = Pattern.compile("[<>;&|]");
        if (regex.matcher(givenString).find())
            return true;
        else
            return false;
    }


    /**
     * @param file
     * @return return file byte data after checking extension
     */
    public static byte[] validateFile(MultipartFile file){
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                logger.info("Sorry! Filename contains invalid path sequence " + fileName);
            }
            return file.getBytes();
        }catch (Exception e){
            logger.error("unable to upload video -> Message: %d ", e);
        }
        return null;
    }
}


