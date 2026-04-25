package com.udit.authlib.service;

import com.udit.authlib.entity.BlacklistedToken;
import com.udit.authlib.repository.BlacklistedTokenRepository;
import com.udit.authlib.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistRepository;
    private final JwtUtils jwtUtils;

    @Transactional
    public void blacklistToken(String token) {
        Date expiry = jwtUtils.getExpirationDateFromToken(token);
        
        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .token(token)
                .expiryDate(expiry.toInstant())
                .build();
                
        blacklistRepository.save(blacklistedToken);
        log.info("Token blacklisted successfully. Expires at: {}", expiry);
    }

    public boolean isBlacklisted(String token) {
        return blacklistRepository.existsByToken(token);
    }

    /**
     * Periodic cleanup of expired blacklisted tokens from the database.
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Running cleanup for expired blacklisted tokens...");
        blacklistRepository.deleteByExpiryDateBefore(Instant.now());
    }
}
