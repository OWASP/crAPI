package com.crapi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LockAccountForm {

  @NotBlank
  @Size(min = 3, max = 100)
  private String email;
}
