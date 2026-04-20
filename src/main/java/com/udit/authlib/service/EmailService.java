package com.udit.authlib.service;

import com.udit.authlib.entity.VerificationToken;

public interface EmailService {

  /**
   * Sends a verification email with a verification link.
   * @param token the verification token
   */
  void sendVerificationEmail(VerificationToken token);

  /**
   * Sends a password reset email with a reset link.
   * @param token the password reset token
   */
  void sendPasswordResetEmail(VerificationToken token);
}

