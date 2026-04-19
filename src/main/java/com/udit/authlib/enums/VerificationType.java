package com.udit.authlib.enums;

import lombok.Getter;

/**
 * Enum to define different types of verification tokens.
 * This allows the same entity to handle multiple verification scenarios
 * while preventing tokens from being misused (e.g., using an email verification
 * token to reset a password).
 */
@Getter
public enum VerificationType {
  EMAIL_VERIFICATION("Email Verification"),
  PASSWORD_RESET("Password Reset"),
  ACCOUNT_UNLOCK("Account Unlock"),
  TWO_FACTOR_AUTH("Two-Factor Authentication");

  private final String displayName;

  VerificationType(String displayName) {
    this.displayName = displayName;
  }
}

