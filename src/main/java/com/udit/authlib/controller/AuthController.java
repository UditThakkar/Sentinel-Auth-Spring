package com.udit.authlib.controller;

import com.udit.authlib.dto.LoginRequest;
import com.udit.authlib.dto.SignupRequest;
import com.udit.authlib.dto.TokenRefreshRequest;
import com.udit.authlib.dto.TokenRefreshResponse;
import com.udit.authlib.repository.UserRepository;
import com.udit.authlib.security.AuthService;
import com.udit.authlib.security.JwtUtils;
import com.udit.authlib.service.VerificationTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${auth-api.baseEndpoint}")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final VerificationTokenService verificationTokenService;

  @PostMapping("${auth-api.signinEndpoint}")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    log.info("API Call: POST /signin - Authentication request for user: {}", loginRequest.getUsername());
    try {
      var response = authService.authenticateUser(loginRequest);
      log.info("API Response: /signin - Authentication successful for user: {}", loginRequest.getUsername());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("API Error: /signin - Authentication failed for user: {} - Error: {}", loginRequest.getUsername(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("${auth-api.signupEndpoint}")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    log.info("API Call: POST /signup - Registration request for username: {}, email: {}", signUpRequest.getUsername(), signUpRequest.getEmail());
    try {
      authService.registerUser(signUpRequest);
      log.info("API Response: /signup - Registration successful for username: {}", signUpRequest.getUsername());
      return ResponseEntity.ok("User registered successfully!");
    } catch (Exception e) {
      log.error("API Error: /signup - Registration failed for username: {} - Error: {}", signUpRequest.getUsername(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("${auth-api.refreshEndpoint}")
  public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
    log.info("API Call: POST /refresh - Token refresh request received");
    try {
      var response = authService.refreshToken(tokenRefreshRequest);
      log.info("API Response: /refresh - Token refresh successful");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("API Error: /refresh - Token refresh failed - Error: {}", e.getMessage());
      throw e;
    }
  }

  @GetMapping("${auth-api.verifyEndpoint}")
  public ResponseEntity<?> verifyToken(@RequestParam String token) {
    log.info("API Call: GET /verify - Verification request received for token: {}", token);
    try {
      verificationTokenService.verifyToken(token);
      log.info("API Response: /verify - Token verified successfully");
      return ResponseEntity.ok("Token verified successfully");
    } catch (Exception e) {
      log.error("API Error: /verify - Token verification failed for token: {}", token);
      throw e;
    }
  }
}