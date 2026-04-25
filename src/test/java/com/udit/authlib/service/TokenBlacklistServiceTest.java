package com.udit.authlib.service;

import com.udit.authlib.entity.BlacklistedToken;
import com.udit.authlib.repository.BlacklistedTokenRepository;
import com.udit.authlib.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private BlacklistedTokenRepository blacklistRepository;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void blacklistTokenPersistsTokenWithJwtExpiry() {
        Date expiry = Date.from(Instant.now().plusSeconds(60));
        when(jwtUtils.getExpirationDateFromToken("jwt-token")).thenReturn(expiry);

        tokenBlacklistService.blacklistToken("jwt-token");

        ArgumentCaptor<BlacklistedToken> tokenCaptor = ArgumentCaptor.forClass(BlacklistedToken.class);
        verify(blacklistRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getToken()).isEqualTo("jwt-token");
        assertThat(tokenCaptor.getValue().getExpiryDate()).isEqualTo(expiry.toInstant());
    }

    @Test
    void isBlacklistedReturnsRepositoryResult() {
        when(blacklistRepository.existsByToken("jwt-token")).thenReturn(true);

        assertThat(tokenBlacklistService.isBlacklisted("jwt-token")).isTrue();
    }

    @Test
    void cleanupExpiredTokensDeletesTokensOlderThanNow() {
        tokenBlacklistService.cleanupExpiredTokens();

        verify(blacklistRepository).deleteByExpiryDateBefore(any(Instant.class));
    }
}
