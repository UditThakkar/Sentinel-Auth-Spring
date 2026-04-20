package com.udit.authlib.service;

import com.udit.authlib.entity.User;
import com.udit.authlib.entity.VerificationToken;
import com.udit.authlib.enums.UserStatus;
import com.udit.authlib.enums.VerificationType;
import com.udit.authlib.properties.AuthProperties;
import com.udit.authlib.repository.UserRepository;
import com.udit.authlib.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class VerificationTokenService {

  private final VerificationTokenRepository repository;
  private final AuthProperties authProperties;
  private final UserRepository userRepository;


  public VerificationToken generateVerificationToken(User user) {
    return generateVerificationToken(user, VerificationType.EMAIL_VERIFICATION);
  }

  /**
   * Generates a verification token for the specified user with a specific type.
   * @param user the user for whom the token is generated
   * @param type the type of verification (EMAIL_VERIFICATION, PASSWORD_RESET, etc.)
   * @return the generated verification token
   */
  public VerificationToken generateVerificationToken(User user, VerificationType type) {
    VerificationToken token = VerificationToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .type(type)
            .expiry(Instant.now().plusMillis(authProperties.getVerificationTokenExpiry()))
            .build();
    VerificationToken savedToken = repository.save(token);
    log.info("Verification token generated successfully for user: {}, type: {}", user.getUsername(), type.getDisplayName());
    return savedToken;
  }

  public void verifyToken(String token) {
    verifyToken(token, VerificationType.EMAIL_VERIFICATION);
  }

  /**
   * Verifies a token and ensures it matches the expected type.
   * This prevents misuse (e.g., using a password reset token for email verification).
   * @param token the token to verify
   * @param expectedType the expected type of verification
   * @throws IllegalArgumentException if token is invalid, expired, or type doesn't match
   */
  public void verifyToken(String token, VerificationType expectedType) {
    VerificationToken vToken = repository.findByToken(token);

    if (vToken == null) {
      log.warn("Invalid verification token");
      throw new IllegalArgumentException("Invalid verification token");
    }

    // Validate token type
    if (vToken.getType() != expectedType) {
      log.warn("Token type mismatch. Expected: {}, Actual: {}", expectedType.getDisplayName(), vToken.getType().getDisplayName());
      throw new IllegalArgumentException("Invalid token type. Expected: " + expectedType.getDisplayName());
    }

    if (vToken.getExpiry().isBefore(Instant.now())) {
      log.warn("Verification token has expired for type: {}", expectedType.getDisplayName());
      throw new IllegalArgumentException("Verification token has expired");
    }

    User user = vToken.getUser();
    user.setStatus(UserStatus.VERIFIED);
    userRepository.save(user);
    log.info("Verification token verified successfully for user: {}, type: {}", user.getUsername(), expectedType.getDisplayName());
    repository.delete(vToken);
  }

  /**
   * Validates a token without performing any state changes.
   * Useful for password reset and other flows where token validation is needed but no user status update is required.
   * @param token the token to validate
   * @param expectedType the expected type of verification
   * @return the associated User if token is valid
   * @throws IllegalArgumentException if token is invalid, expired, or type doesn't match
   */
  public User validateTokenAndGetUser(String token, VerificationType expectedType) {
    VerificationToken vToken = repository.findByToken(token);

    if (vToken == null) {
      log.warn("Invalid verification token");
      throw new IllegalArgumentException("Invalid verification token");
    }

    // Validate token type
    if (vToken.getType() != expectedType) {
      log.warn("Token type mismatch. Expected: {}, Actual: {}", expectedType.getDisplayName(), vToken.getType().getDisplayName());
      throw new IllegalArgumentException("Invalid token type. Expected: " + expectedType.getDisplayName());
    }

    if (vToken.getExpiry().isBefore(Instant.now())) {
      log.warn("Verification token has expired for type: {}", expectedType.getDisplayName());
      throw new IllegalArgumentException("Verification token has expired");
    }

    log.info("Token validated successfully for user: {}, type: {}", vToken.getUser().getUsername(), expectedType.getDisplayName());
    return vToken.getUser();
  }

  /**
   * Deletes a verification token by token string.
   * Used after token has been consumed (e.g., after password reset).
   * @param token the token string to delete
   */
  public void deleteToken(String token) {
    VerificationToken vToken = repository.findByToken(token);
    if (vToken != null) {
      repository.delete(vToken);
      log.info("Verification token deleted for user: {}, type: {}", vToken.getUser().getUsername(), vToken.getType().getDisplayName());
    } else {
      log.warn("Attempted to delete non-existent token");
    }
  }
}
