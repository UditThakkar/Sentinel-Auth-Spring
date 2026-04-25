package com.udit.authlib.service;

import com.udit.authlib.entity.User;
import com.udit.authlib.entity.VerificationToken;
import com.udit.authlib.enums.UserStatus;
import com.udit.authlib.enums.VerificationType;
import com.udit.authlib.properties.AuthProperties;
import com.udit.authlib.repository.UserRepository;
import com.udit.authlib.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @Mock
    private VerificationTokenRepository repository;
    @Mock
    private AuthProperties authProperties;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .status(UserStatus.UNVERIFIED)
                .build();
    }

    @Test
    void generateVerificationTokenPersistsTokenWithRequestedTypeAndExpiry() {
        when(authProperties.getVerificationTokenExpiry()).thenReturn(3_600_000L);
        when(repository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken token = verificationTokenService.generateVerificationToken(user, VerificationType.PASSWORD_RESET);

        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getUser()).isSameAs(user);
        assertThat(token.getType()).isEqualTo(VerificationType.PASSWORD_RESET);
        assertThat(token.getExpiry()).isAfter(Instant.now().plusSeconds(3_500));
        verify(repository).save(token);
    }

    @Test
    void verifyTokenMarksUserVerifiedAndDeletesToken() {
        VerificationToken token = VerificationToken.builder()
                .token("verification-token")
                .user(user)
                .type(VerificationType.EMAIL_VERIFICATION)
                .expiry(Instant.now().plusSeconds(60))
                .build();
        when(repository.findByToken("verification-token")).thenReturn(token);

        verificationTokenService.verifyToken("verification-token");

        assertThat(user.getStatus()).isEqualTo(UserStatus.VERIFIED);
        verify(userRepository).save(user);
        verify(repository).delete(token);
    }

    @Test
    void verifyTokenRejectsWrongTokenTypeWithoutStateChanges() {
        VerificationToken token = VerificationToken.builder()
                .token("reset-token")
                .user(user)
                .type(VerificationType.PASSWORD_RESET)
                .expiry(Instant.now().plusSeconds(60))
                .build();
        when(repository.findByToken("reset-token")).thenReturn(token);

        assertThatThrownBy(() -> verificationTokenService.verifyToken("reset-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid token type. Expected: Email Verification");

        assertThat(user.getStatus()).isEqualTo(UserStatus.UNVERIFIED);
        verify(userRepository, never()).save(any(User.class));
        verify(repository, never()).delete(any());
    }

    @Test
    void validateTokenAndGetUserReturnsUserWithoutUpdatingVerificationStatus() {
        VerificationToken token = VerificationToken.builder()
                .token("reset-token")
                .user(user)
                .type(VerificationType.PASSWORD_RESET)
                .expiry(Instant.now().plusSeconds(60))
                .build();
        when(repository.findByToken("reset-token")).thenReturn(token);

        User result = verificationTokenService.validateTokenAndGetUser("reset-token", VerificationType.PASSWORD_RESET);

        assertThat(result).isSameAs(user);
        assertThat(user.getStatus()).isEqualTo(UserStatus.UNVERIFIED);
        verify(userRepository, never()).save(any(User.class));
        verify(repository, never()).delete(any());
    }

    @Test
    void expiredTokenIsRejected() {
        VerificationToken token = VerificationToken.builder()
                .token("expired-token")
                .user(user)
                .type(VerificationType.EMAIL_VERIFICATION)
                .expiry(Instant.now().minusSeconds(1))
                .build();
        when(repository.findByToken("expired-token")).thenReturn(token);

        assertThatThrownBy(() -> verificationTokenService.verifyToken("expired-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Verification token has expired");

        verify(userRepository, never()).save(any(User.class));
        verify(repository, never()).delete(any());
    }

    @Test
    void deleteTokenDeletesExistingTokenOnly() {
        VerificationToken token = VerificationToken.builder()
                .token("reset-token")
                .user(user)
                .type(VerificationType.PASSWORD_RESET)
                .expiry(Instant.now().plusSeconds(60))
                .build();
        when(repository.findByToken("reset-token")).thenReturn(token);

        verificationTokenService.deleteToken("reset-token");

        verify(repository).delete(token);
    }

    @Test
    void deleteTokenIgnoresMissingToken() {
        when(repository.findByToken("missing-token")).thenReturn(null);

        verificationTokenService.deleteToken("missing-token");

        verify(repository, never()).delete(any());
    }
}
