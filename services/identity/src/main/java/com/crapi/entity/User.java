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

import com.crapi.enums.ERole;
import com.crapi.enums.EStatus;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;


/**
 * @author Traceable AI
 */
@Entity
@Table(name="user_login")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="user_generator")
    @SequenceGenerator(name="user_generator", sequenceName = "user_login_id_seq", allocationSize = 1)
    private Long id;
    private String email;
    private String password;
    private String number;
    @Column(length = 500)
    private String jwtToken;

    private LocalDate createdOn = LocalDate.now();

    //@OneToOne
    private ERole role;

    public User(){

    }
    public User(String email, String number, String password, ERole userRole) {

        this.password = password;
        this.email = email;
        this.number = number;
        this.role = userRole;

    }

}
