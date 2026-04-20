package com.udit.authlib.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
  
  @NotBlank(message = "Token is required")
  private String token;
  
  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password should be at least 6 characters long")
  private String newPassword;
}

