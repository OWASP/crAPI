package com.crapi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnlockAccountForm {

  @NotBlank
  @Size(min = 8, max = 8)
  private String mfaCode;
}
