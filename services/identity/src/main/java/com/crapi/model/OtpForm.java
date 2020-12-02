package com.crapi.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Traceabel AI
 */
@Data
public class OtpForm {
    @NotBlank
    @Size(min=3, max=4)
    private String otp;
    @NotBlank
    @Size(min=5, max=30)
    private String password;

    @NotBlank
    @Size(min=5, max=30)
    private String email;


}
