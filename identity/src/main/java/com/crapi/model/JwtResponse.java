package com.crapi.model;

import lombok.Data;

/**
 * @author Traceabel AI
 */
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String message;

    public JwtResponse(){

    }
    public JwtResponse(String accessToken) {
        this.token = accessToken;
    }

    public JwtResponse(String token,String message) {
        this.token = token;
        this.type ="";
        this.message = message;
    }



}
