package com.udit.authlib.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException e) {
    log.error("Exception Caught: UserAlreadyExistsException - {}", e.getMessage());
    ErrorResponse error = new ErrorResponse("USER_ALREADY_EXISTS", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(UserLockedException.class)
  public ResponseEntity<ErrorResponse> handleUserLocked(UserLockedException e) {
    log.error("Exception Caught: UserLockedException - {}", e.getMessage());
    ErrorResponse error = new ErrorResponse("ACCOUNT_LOCKED", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.status(423).body(error);  // 423 Locked
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
    log.warn("Exception Caught: BadCredentialsException - {}", e.getMessage());
    ErrorResponse error = new ErrorResponse("BAD_CREDENTIALS", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.status(401).body(error);
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ErrorResponse> handleDisabledAccount(DisabledException e) {
    log.warn("Exception Caught: DisabledException - Account is disabled. User must verify their email before logging in.");
    ErrorResponse error = new ErrorResponse("EMAIL_NOT_VERIFIED", "Your account is disabled. Please verify your email to activate your account.", System.currentTimeMillis());
    return ResponseEntity.status(403).body(error);  // 403 Forbidden
  }

  @ExceptionHandler(RefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleRefreshToken(RefreshTokenException e) {
    log.error("Exception Caught: RefreshTokenException - {}", e.getMessage());
    ErrorResponse error = new ErrorResponse("REFRESH_TOKEN_INVALID", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.status(401).body(error);
  }

  @ExceptionHandler(VerificationTokenException.class)
  public ResponseEntity<ErrorResponse> handleVerificationToken(VerificationTokenException e) {
    log.error("Exception Caught: VerificationTokenException - {}", e.getMessage());
    ErrorResponse error = new ErrorResponse("VERIFICATION_TOKEN_INVALID", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.status(401).body(error);
  }
}
