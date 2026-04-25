package com.udit.authlib.security;

import com.udit.authlib.entity.RefreshToken;
import com.udit.authlib.entity.User;
import com.udit.authlib.exception.RefreshTokenException;
import com.udit.authlib.properties.AuthProperties;
import com.udit.authlib.repository.RefreshTokenRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .build();
    }

    @Test
    void generateRefreshTokenDeletesExistingTokenAndPersistsNewOne() {
        when(authProperties.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(refreshToken).isSameAs(tokenCaptor.getValue());
        assertThat(refreshToken.getUser()).isSameAs(user);
        assertThat(refreshToken.getToken()).isNotBlank();
        assertThat(refreshToken.getExpiryDate()).isAfter(Instant.now().plusSeconds(604_700));
    }

    @Test
    void validateRefreshTokenReturnsNonExpiredToken() {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("refresh-token")
                .expiryDate(Instant.now().plusSeconds(60))
                .build();
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(refreshToken);

        RefreshToken result = refreshTokenService.validateRefreshToken("refresh-token");

        assertThat(result).isSameAs(refreshToken);
    }

    @Test
    void validateRefreshTokenThrowsWhenTokenDoesNotExist() {
        when(refreshTokenRepository.findByToken("missing-token")).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("missing-token"))
                .isInstanceOf(RefreshTokenException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void validateRefreshTokenDeletesAndRejectsExpiredToken() {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(1))
                .build();
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(refreshToken);

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("expired-token"))
                .isInstanceOf(RefreshTokenException.class)
                .hasMessage("Refresh token has expired");

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void deleteByUserDeletesRefreshTokensForUser() {
        refreshTokenService.deleteByUser(user);

        verify(refreshTokenRepository).deleteByUser(user);
    }
}
