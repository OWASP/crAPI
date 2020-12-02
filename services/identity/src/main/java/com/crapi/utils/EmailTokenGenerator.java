package com.crapi.utils;

import java.util.Random;
/**
 * @author Traceabel AI
 */
public class EmailTokenGenerator {
    static String charsequence="abcdefghijklmnopqrstuvwxzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static String num ="0123456789";
    String url="";


    /**
     * @param length
     * @return generate random string for email token and magic url
     */
    public  String generateRandom(int length){
        for (int i=0; i<length; i++){
            url+=randomCharacter(charsequence);
            url+=randomNumber(num);
        }
        return url;
    }

    public  String randomCharacter(String characters){
        int n = characters.length();
        int r = (int)(n*Math.random());
        return  characters.substring(r,r+1);
    }
    public  String randomNumber(String characters){
        int n = num.length();
        int r = (int)(n*Math.random());
        return  num.substring(r,r+1);
    }


}
