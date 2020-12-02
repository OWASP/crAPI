package com.crapi.utils;

/**
 * @author Traceabel AI
 */

public class GenerateVIN {

    static String charsequence="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static String num ="0123456789";
    String vin ="";
    String pincode ="";

    /**
     * @return random generate pin code for add vehicle
     */
    public String generatePincode(){
        pincode+=getNum(3);
        return pincode;
    }

    /**
     * @return rendom generate VIN for vehicle
     */
    public String generateVIN(){
            vin+= getNum(0)+getChar(3)+getNum(1)+getChar(3)+getNum(5);
        return vin;
    }
    public String getChar(int num){
        String random="";
        for (int j=0;j<=num; j++)
            random +=randomCharacter();
        return random;
    }
    public String getNum(int num){
        String random="";
        for (int k=0;k<=num; k++)
            random +=randomNumber();
        return random;
    }

    public String randomCharacter(){
        int n = charsequence.length();
        int r = (int) (n * Math.random());
        return charsequence.substring(r, r + 1);
    }
    public  String randomNumber(){
        int n = num.length();
        int r = (int)(n*Math.random());
        return  num.substring(r,r+1);
    }
}
