package com.preeti.sansarcart.service;

import com.preeti.sansarcart.entity.BlacklistedToken;
import com.preeti.sansarcart.repository.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklist(String token, long expiryMillis) {
        Instant expiry = Instant.ofEpochMilli(expiryMillis);
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpiry(expiry);
        blacklistedTokenRepository.save(blacklistedToken);
    }

    public boolean isBlacklisted(String token) {
        Optional<BlacklistedToken> blacklistedToken = blacklistedTokenRepository.findByToken(token);
        if (blacklistedToken.isPresent()) {
            if (blacklistedToken.get().isExpired()) {
                blacklistedTokenRepository.delete(blacklistedToken.get());
                return false;
            }
            return true;
        }
        return false;
    }

    @Transactional
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void cleanUpExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklistedTokenRepository.deleteExpiredTokens(Instant.ofEpochMilli(now));
    }
}

