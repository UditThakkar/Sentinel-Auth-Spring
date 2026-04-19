package com.udit.authlib.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginRequest {

  @NotBlank
  private String username;

  @NotBlank
  private String password;

}
