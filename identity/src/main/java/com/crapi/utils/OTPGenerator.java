package com.crapi.utils;

import org.springframework.stereotype.Component;
/**
 * @author Traceabel AI
 */
@Component
public class OTPGenerator {

     String num ="0123456789";
     String otp="";


    /**
     * @param length
     * @return generate random otp for forgot password
     */
    public  String generateRandom(int length){
            for (int i = 0; i < length; i++) {
                otp += randomNumber(num);
            }

        return otp;
    }
    public  String randomNumber(String characters){
        int n = num.length();
        int r = (int)(n*Math.random());
        return  num.substring(r,r+1);
    }
}
