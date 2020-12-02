package com.crapi.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Traceabel AI
 */
@Data
public class LoginForm {

    @NotBlank
    @Size(min=3, max = 60)
    private String email;

    @NotBlank
    @Size(min = 4, max = 40)
    private String password;

    private String number;

}
