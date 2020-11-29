package com.crapi.model;

import lombok.Data;

@Data
public class ErrorDetails {

    private String message;
    private String details;
    public ErrorDetails(){}

            public ErrorDetails( String message, String details) {
        super();
        this.message = message;
        this.details = details;
    }
}
