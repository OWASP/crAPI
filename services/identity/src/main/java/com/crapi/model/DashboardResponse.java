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

@Data
public class DashboardResponse {
    private long id;
    private String name;

    private String email;

    private String number;
    private String picture_url;
    private String video_url;
    private String video_name;
    private double available_credit;
    private long video_id;

    private String role;



    public DashboardResponse(Long id,String name, String email,String number, String role, double available_credit){
        this.id =id;
        this.name = name;
        this.email=email;
        this.number = number;
        this.role = role;
        this.available_credit=available_credit;


    }
}
