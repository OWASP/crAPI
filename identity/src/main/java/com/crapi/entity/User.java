package com.crapi.entity;

import com.crapi.enums.ERole;
import com.crapi.enums.EStatus;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;


/**
 * @author Traceabel AI
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
