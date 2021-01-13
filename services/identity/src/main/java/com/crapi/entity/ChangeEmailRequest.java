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

package com.crapi.entity;

import com.crapi.enums.EStatus;
import lombok.Data;

import javax.persistence.*;

/**
 * @author Traceable AI
 */

@Entity
@Table(name = "otp_token")
@Data
public class ChangeEmailRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "new_email")
    private String newEmail;
    @Column(name = "old_email")
    private String oldEmail;
    @Column(name = "email_token")
    private String emailToken;
    private String status;
    
    @OneToOne
    private User user;



    public ChangeEmailRequest(){

    }
    public ChangeEmailRequest(String new_email,String oldEmail, String token, User user){
        this.newEmail = new_email;
        this.emailToken = token;
        this.oldEmail = oldEmail;
        this.user = user;
        this.status = EStatus.ACTIVE.toString();
    }


}
