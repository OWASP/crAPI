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