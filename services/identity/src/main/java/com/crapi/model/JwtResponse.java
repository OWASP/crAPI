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

package com.crapi.model;

import lombok.Data;

/**
 * @author Traceable AI
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
