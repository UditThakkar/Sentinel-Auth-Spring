package com.udit.authlib.service;

import com.udit.authlib.dto.EmailModel;
import com.udit.authlib.entity.VerificationToken;
import org.springframework.stereotype.Service;

public class DefaultEmailTemplateProvider implements EmailTemplateProvider{
  @Override
  public EmailModel buildVerificationEmail(VerificationToken token, String link) {
    String htmlContent = "<h2>Welcome to AuthLib!</h2>" +
            "<p>Please click the link below to verify your email address:</p>" +
            "<a href=\"" + link + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: #ffffff; background-color: #007bff; text-decoration: none; border-radius: 5px;\">Verify Email</a>" +
            "<p>If you didn't create an account, please ignore this email.</p>";

    return EmailModel.builder()
            .subject("Verify Your Email Address")
            .body(htmlContent)
            .build();
  }

  @Override
  public EmailModel buildPasswordResetEmail(VerificationToken token, String link) {
    String htmlContent = "<h2>Password Reset</h2>" +
            "<p>You recently requested to reset your password. Click the link below to reset it:</p>" +
            "<a href=\"" + link + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: #ffffff; background-color: #dc3545; text-decoration: none; border-radius: 5px;\">Reset Password</a>" +
            "<p>If you didn't request this, please ignore this email.</p>";

    return EmailModel.builder()
            .subject("Password Reset Request")
            .body(htmlContent)
            .build();
  }
}
