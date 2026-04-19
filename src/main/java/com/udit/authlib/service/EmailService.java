package com.udit.authlib.service;

import com.udit.authlib.entity.VerificationToken;
import com.udit.authlib.enums.VerificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  /**
   * Sends a verification email with a verification link.
   * @param token the verification token
   */
  public void sendVerificationEmail(VerificationToken token) {
    String link = "http://localhost:8080/api/auth/verify?token=" + token.getToken();
    log.info("Sending verification email with link: {}", link);
    // TODO: Implement actual email sending logic
  }

  /**
   * Sends a password reset email with a reset link.
   * @param token the password reset token
   */
  public void sendPasswordResetEmail(VerificationToken token) {
    if (token.getType() != VerificationType.PASSWORD_RESET) {
      log.warn("Attempted to send password reset email with non-reset token type: {}", token.getType());
      throw new IllegalArgumentException("Token is not a password reset token");
    }
    String link = "http://localhost:8080/api/auth/reset-password?token=" + token.getToken();
    log.info("Sending password reset email to user: {} with link: {}", token.getUser().getEmail(), link);
    // TODO: Implement actual email sending logic
  }
}

