package com.udit.authlib.controller;

import com.udit.authlib.dto.LoginRequest;
import com.udit.authlib.dto.SignupRequest;
import com.udit.authlib.repository.UserRepository;
import com.udit.authlib.security.AuthService;
import com.udit.authlib.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${auth-api.baseEndpoint}")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("${auth-api.signinEndpoint}")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.authenticateUser(loginRequest));
  }

  @PostMapping("${auth-api.signupEndpoint}")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    authService.registerUser(signUpRequest);
    return ResponseEntity.ok("User registered successfully!");
  }
}