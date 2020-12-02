package com.crapi.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Traceabel AI
 */

@Data
public class ChangeEmailForm {


    @NotBlank
    @Size(min=3, max = 40)
    private String old_email;
    @NotBlank
    @Size(min=3, max = 40)
    private String new_email;
    private String token;
}
