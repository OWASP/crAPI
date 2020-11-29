package com.crapi.entity;

import com.crapi.enums.EStatus;
import lombok.Data;

import javax.persistence.*;

/**
 * @author Traceabel AI
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
