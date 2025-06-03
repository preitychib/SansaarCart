package com.preeti.sansarcart.service;

import com.preeti.sansarcart.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void removeExpiredTokens() {
        tokenRepository.deleteByExpiryBefore(Instant.now());
    }
}
