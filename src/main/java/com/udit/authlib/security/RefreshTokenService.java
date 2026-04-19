package com.udit.authlib.security;

import com.udit.authlib.entity.RefreshToken;
import com.udit.authlib.entity.User;
import com.udit.authlib.exception.RefreshTokenException;
import com.udit.authlib.properties.AuthProperties;
import com.udit.authlib.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;

    public RefreshToken generateRefreshToken(User user) {
        log.debug("Generating new refresh token for user: {}", user.getUsername());
        refreshTokenRepository.deleteByUser(user); // Delete existing tokens for the user
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(authProperties.getRefreshTokenExpirationMs()));
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token generated successfully for user: {} - Token expires in: {} ms", user.getUsername(), authProperties.getRefreshTokenExpirationMs());
        return savedToken;
    }

    public RefreshToken validateRefreshToken(String token) {
        log.debug("Validating refresh token");
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken == null) {
            log.warn("Invalid refresh token - token not found in database");
            throw new RefreshTokenException("Invalid refresh token");
        }
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Refresh token has expired for user: {}", refreshToken.getUser().getUsername());
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token has expired");
        }
        log.info("Refresh token validated successfully for user: {}", refreshToken.getUser().getUsername());
        return refreshToken;
    }
}
