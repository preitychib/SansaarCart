package com.preeti.sansarcart.service;

import com.preeti.sansarcart.entity.RefreshToken;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.repository.RefreshTokenRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Getter
    @Value("${security.jwt.refresh-token.expiration-ms}")
    public Long refreshTokenDurationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshToken createRefreshToken(User user) {
        String token = generateSecureToken();
        Instant expiryDate = Instant.now().plusMillis(refreshTokenDurationMs);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public boolean isValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .isPresent();
    }

    public RefreshToken getValidToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .orElseThrow(() -> new RuntimeException("Refresh token invalid or expired"));
    }

    public void invalidateToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    public void invalidateAllTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}

