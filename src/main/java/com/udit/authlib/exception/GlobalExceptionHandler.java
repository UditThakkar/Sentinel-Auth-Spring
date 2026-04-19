package com.udit.authlib.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

  @ExceptionHandler(RefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleRefreshToken(RefreshTokenException e) {
    log.error("Exception Caught: RefreshTokenException - {}", e.getMessage());
    ErrorResponse error = new ErrorResponse("REFRESH_TOKEN_INVALID", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.status(401).body(error);
  }
}
