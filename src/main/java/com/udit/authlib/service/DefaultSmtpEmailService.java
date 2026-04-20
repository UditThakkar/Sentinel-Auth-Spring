package com.udit.authlib.service;

import com.udit.authlib.entity.VerificationToken;
import com.udit.authlib.enums.VerificationType;
import com.udit.authlib.properties.AuthProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultSmtpEmailService implements EmailService {

  private final JavaMailSender mailSender;
  private final AuthProperties authProperties;

  @Async
  @Override
  public void sendVerificationEmail(VerificationToken token) {
    String link = authProperties.getFrontendUrl() + authProperties.getBaseEndpoint() + authProperties.getVerifyEndpoint() + "?token=" + token.getToken();
    log.info("Sending verification email with link: {}", link);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      
      helper.setTo(token.getUser().getEmail());
      helper.setSubject("Verify Your Email Address");
      
      String htmlContent = "<h2>Welcome to AuthLib!</h2>" +
               "<p>Please click the link below to verify your email address:</p>" +
               "<a href=\"" + link + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: #ffffff; background-color: #007bff; text-decoration: none; border-radius: 5px;\">Verify Email</a>" +
               "<p>If you didn't create an account, please ignore this email.</p>";
               
      helper.setText(htmlContent, true);
      mailSender.send(message);
      log.info("Verification email sent successfully to {}", token.getUser().getEmail());
    } catch (MessagingException e) {
      log.error("Failed to send verification email to {}", token.getUser().getEmail(), e);
    }
  }

  @Async
  @Override
  public void sendPasswordResetEmail(VerificationToken token) {
    if (token.getType() != VerificationType.PASSWORD_RESET) {
      log.warn("Attempted to send password reset email with non-reset token type: {}", token.getType());
      throw new IllegalArgumentException("Token is not a password reset token");
    }
    String link = authProperties.getFrontendUrl() + authProperties.getBaseEndpoint() + authProperties.getResetPasswordEndpoint() + "?token=" + token.getToken();
    log.info("Sending password reset email to user: {} with link: {}", token.getUser().getEmail(), link);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      
      helper.setTo(token.getUser().getEmail());
      helper.setSubject("Password Reset Request");
      
      String htmlContent = "<h2>Password Reset</h2>" +
               "<p>You recently requested to reset your password. Click the link below to reset it:</p>" +
               "<a href=\"" + link + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: #ffffff; background-color: #dc3545; text-decoration: none; border-radius: 5px;\">Reset Password</a>" +
               "<p>If you didn't request this, please ignore this email.</p>";
               
      helper.setText(htmlContent, true);
      mailSender.send(message);
      log.info("Password reset email sent successfully to {}", token.getUser().getEmail());
    } catch (MessagingException e) {
      log.error("Failed to send password reset email to {}", token.getUser().getEmail(), e);
    }
  }
}
