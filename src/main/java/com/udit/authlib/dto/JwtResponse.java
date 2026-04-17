package com.udit.authlib.dto;

import com.udit.authlib.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JwtResponse {

  private String token;
  private String username;
  private List<Role> roles;
}
