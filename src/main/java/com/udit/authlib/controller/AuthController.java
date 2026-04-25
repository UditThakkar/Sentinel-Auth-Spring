package com.udit.authlib.controller;

import com.udit.authlib.dto.LoginRequest;
import com.udit.authlib.dto.SignupRequest;
import com.udit.authlib.dto.TokenRefreshRequest;
import com.udit.authlib.dto.ForgotPasswordRequest;
import com.udit.authlib.dto.ResetPasswordRequest;
import com.udit.authlib.entity.User;
import com.udit.authlib.security.AuthService;
import com.udit.authlib.service.VerificationTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${auth-api.baseEndpoint}")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final VerificationTokenService verificationTokenService;

  @PostMapping("${auth-api.signinEndpoint}")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
    log.info("API Call: POST /signin - Authentication request for user: {}", loginRequest.getUsername());
    try {
      var response = authService.authenticateUser(loginRequest, request.getRemoteAddr());
      log.info("API Response: /signin - Authentication successful for user: {}", loginRequest.getUsername());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("API Error: /signin - Authentication failed for user: {} - Error: {}", loginRequest.getUsername(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("${auth-api.signupEndpoint}")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, HttpServletRequest request) {
    log.info("API Call: POST /signup - Registration request for username: {}, email: {}", signUpRequest.getUsername(), signUpRequest.getEmail());
    try {
      authService.registerUser(signUpRequest, request.getRemoteAddr());
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

  @PostMapping("${auth-api.forgotPasswordEndpoint}")
  public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request) {
    log.info("API Call: POST /forgot-password - Password reset request for email: {}", forgotPasswordRequest.getEmail());
    try {
      authService.requestPasswordReset(forgotPasswordRequest.getEmail(), request.getRemoteAddr());
      log.info("API Response: /forgot-password - Password reset request processed");
      // Always return the same message to prevent user enumeration attacks
      return ResponseEntity.ok("If the email exists in our system, you will receive a password reset link shortly");
    } catch (Exception e) {
      log.error("API Error: /forgot-password - Password reset request failed for email: {} - Error: {}", forgotPasswordRequest.getEmail(), e.getMessage());
      throw e;
    }
  }

  @PostMapping("${auth-api.resetPasswordEndpoint}")
  public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
    log.info("API Call: POST /reset-password - Password reset with token");
    try {
      authService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword(), request.getRemoteAddr());
      log.info("API Response: /reset-password - Password reset successful");
      return ResponseEntity.ok("Password has been reset successfully. You can now log in with your new password.");
    } catch (Exception e) {
      log.error("API Error: /reset-password - Password reset failed - Error: {}", e.getMessage());
      throw e;
    }
  }

  @PostMapping("${auth-api.logoutEndpoint}")
  public ResponseEntity<?> logout(@AuthenticationPrincipal User user, HttpServletRequest request) {
    log.info("API Call: POST /logout - Logout request for user: {}", user.getUsername());
    try {
      String jwt = parseJwt(request);
      authService.logout(jwt, user, request.getRemoteAddr());
      log.info("API Response: /logout - Logout successful");
      return ResponseEntity.ok("User logged out successfully");
    } catch (Exception e) {
      log.error("API Error: /logout - Logout failed - Error: {}", e.getMessage());
      throw e;
    }
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (org.springframework.util.StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }
}
