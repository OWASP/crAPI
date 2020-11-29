package com.crapi.exception;
import lombok.Data;

@Data
public class CRAPIExceptionHandler extends RuntimeException {
    private String message;
    private String details;
    private int status;
     
    public int getStatus() {
        return status;
    }
     
    public void setStatus(int status) {
        this.status = status;
    }
     
    public String getDetails() {
        return details;
    }
     
    public void setDetails(String details) {
        this.details = details;
    }
     
    public String getMessage() {
        return message;
    }
     
    public void setMessage(String message) {
        this.message = message;
    }

    protected CRAPIExceptionHandler() {}

    public CRAPIExceptionHandler(String message,int status) {
        super();
        this.message = message;
        this.status = status;

    }
    public CRAPIExceptionHandler(String message,String details, int status) {
        super();
        this.message = message;
        this.details = details;
        this.status = status;
    }
}