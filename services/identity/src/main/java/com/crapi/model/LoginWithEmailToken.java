package com.crapi.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Traceabel AI
 */

@Data
public class LoginWithEmailToken {

    @NotBlank
    @Size(min=3, max = 60)
    private String email;

    @NotBlank
    @Size(min=3, max = 60)
    private String token;

}
