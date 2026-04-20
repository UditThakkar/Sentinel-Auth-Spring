package com.udit.authlib.service;

import com.udit.authlib.entity.VerificationToken;
import com.udit.authlib.enums.VerificationType;
import com.udit.authlib.properties.AuthProperties;
import com.udit.authlib.dto.EmailModel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

@RequiredArgsConstructor
@Slf4j
public class DefaultSmtpEmailService implements EmailService {
  @Value("${spring.mail.username}")
  private String mailUsername;

  private final JavaMailSender mailSender;
  private final AuthProperties authProperties;
  private final EmailTemplateProvider templateProvider;

  @Async
  @Override
  public void sendVerificationEmail(VerificationToken token) {
    String link = buildLink(authProperties.getVerifyEndpoint(), token.getToken());
    log.info("Sending verification email with link: {}", link);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      
      setSenderFromProperty(helper);
      helper.setTo(token.getUser().getEmail());

      EmailModel emailModel = templateProvider.buildVerificationEmail(token, link);
      helper.setSubject(emailModel.getSubject());
      helper.setText(emailModel.getBody(), true);
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
    String link = buildLink(authProperties.getResetPasswordEndpoint(), token.getToken());
    log.info("Sending password reset email to user: {} with link: {}", token.getUser().getEmail(), link);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      
      setSenderFromProperty(helper);
      helper.setTo(token.getUser().getEmail());

      EmailModel emailModel = templateProvider.buildPasswordResetEmail(token, link);
      helper.setSubject(emailModel.getSubject());
      helper.setText(emailModel.getBody(), true);
      mailSender.send(message);
      log.info("Password reset email sent successfully to {}", token.getUser().getEmail());
    } catch (MessagingException e) {
      log.error("Failed to send password reset email to {}", token.getUser().getEmail(), e);
    }
  }

  /**
   * Builds the email link with frontend URL, endpoint, and token.
   *
   * @param endpoint the endpoint (e.g., /verify, /reset-password)
   * @param token the verification or reset token
   * @return the complete link URL
   */
  private String buildLink(String endpoint, String token) {
    return authProperties.getFrontendUrl() + authProperties.getBaseEndpoint() + endpoint + "?" + authProperties.getTokenParamName() + "=" + token;
  }

  /**
   * Sets the sender email address with the display name from AuthProperties.
   * Falls back to email-only if display name encoding fails.
   *
   * @param helper the MimeMessageHelper to configure
   * @throws MessagingException if setting the sender fails
   */
  private void setSenderFromProperty(MimeMessageHelper helper) throws MessagingException {
    try {
      helper.setFrom(mailUsername, authProperties.getEmailFromName());
    } catch (java.io.UnsupportedEncodingException e) {
      log.warn("Failed to set sender name, falling back to email only", e);
      helper.setFrom(mailUsername);
    }
  }
}
