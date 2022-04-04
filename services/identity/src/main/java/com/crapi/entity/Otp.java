/*
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

import lombok.Data;

import javax.persistence.*;


@Entity
@Table(name = "otp")
@Data
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String otp;
    private String status;
    private int count;
    
    @OneToOne
    private User user;

    public Otp(){

    }

    public Otp(String otp, User user){
        this.otp = otp;
        this.user = user;

    }

}
