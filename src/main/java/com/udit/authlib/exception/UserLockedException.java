package com.udit.authlib.exception;

public class UserLockedException extends RuntimeException {
  public UserLockedException(String message) {
    super(message);
  }
}
