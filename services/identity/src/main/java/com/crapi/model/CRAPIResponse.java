package com.crapi.model;

import lombok.Data;

/**
 * @author Traceabel AI
 */

@Data
public class CRAPIResponse {

    private String message;
    private int status;



    public CRAPIResponse(){

    }

    public CRAPIResponse(String message){
        this.message = message;
    }

    public CRAPIResponse(String message, int status){
        this.message = message;
        this.status = status;
    }

}
