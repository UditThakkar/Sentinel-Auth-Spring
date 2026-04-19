package com.udit.authlib.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SignupRequest {
  private String username;

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String password;

  private String firstName;
  private String lastName;
}
