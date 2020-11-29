package com.crapi.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Traceabel AI
 */
@Data
public class SignUpForm {
    private Long id;

    @NotBlank
    @Size(min = 3, max = 40)
    private String name;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    @Size(max = 60)
    @Email
    private String email;

    @NotBlank
    @Size(max = 15)
    private String number;


    public SignUpForm(Long id,String name, String email,String number){
        this.id =id;
        this.name = name;
        this.email=email;
        this.number = number;


    }
}
