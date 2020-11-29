package com.crapi.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class ForgetPassword implements Serializable {

    @NotBlank
    @Size(min=3, max = 60)
    @Email
   private String email;


}
