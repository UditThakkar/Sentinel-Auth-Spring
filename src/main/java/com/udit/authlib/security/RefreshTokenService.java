package com.udit.authlib.security;

import com.udit.authlib.entity.RefreshToken;
import com.udit.authlib.entity.User;
import com.udit.authlib.exception.RefreshTokenException;
import com.udit.authlib.properties.AuthProperties;
import com.udit.authlib.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;

    public RefreshToken generateRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user); // Delete existing tokens for the user
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(authProperties.getRefreshTokenExpirationMs()));
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken == null) {
            throw new RefreshTokenException("Invalid refresh token");
        }
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("Refresh token has expired");
        }
        return refreshToken;
    }
}
