package com.udit.authlib.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException e) {
    ErrorResponse error = new ErrorResponse("USER_ALREADY_EXISTS", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(UserLockedException.class)
  public ResponseEntity<ErrorResponse> handleUserLocked(UserLockedException e) {
    ErrorResponse error = new ErrorResponse("ACCOUNT_LOCKED", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.status(423).body(error);  // 423 Locked
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
    ErrorResponse error = new ErrorResponse("BAD_CREDENTIALS", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.status(401).body(error);
  }

  @ExceptionHandler(RefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleRefreshToken(RefreshTokenException e) {
    ErrorResponse error = new ErrorResponse("REFRESH_TOKEN_INVALID", e.getMessage(), System.currentTimeMillis());
    return ResponseEntity.status(401).body(error);
  }
}
