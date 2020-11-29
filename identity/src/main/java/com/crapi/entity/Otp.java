package com.crapi.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Traceabel AI
 */

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
