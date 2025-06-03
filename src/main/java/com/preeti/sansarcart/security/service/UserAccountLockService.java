package com.preeti.sansarcart.security.service;

import com.preeti.sansarcart.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountLockService {

    private final UserRepository userRepository;

    private final Map<String, Instant> lockedUsers = new ConcurrentHashMap<>();

    @Value("${app.login.unlock.minutes:60}")
    private long unlockMinutes;

    public void lockUser(String email) {
        lockedUsers.put(email, Instant.now());
    }

    public void unlockUser(String email) {
        lockedUsers.remove(email);
    }

    public boolean isLocked(String email) {
        return lockedUsers.containsKey(email);
    }

    public Instant getLockedAt(String email) {
        return lockedUsers.get(email);
    }

    public Map<String, Instant> getAllLockedUsers() {
        return Collections.unmodifiableMap(lockedUsers);
    }

    @Scheduled(fixedRate = 60000)
    public void unlockExpiredAccounts() {
        Instant now = Instant.now();

        lockedUsers.forEach((email, lockedAt) -> {
            if (Duration.between(lockedAt, now).toMinutes() >= unlockMinutes) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    user.setInvalidAttemptCount(0);
                    user.setAccountLocked(false);
                    userRepository.save(user);
                    unlockUser(email);
                    log.info("User {} unlocked automatically", email);
                });
            }
        });
    }
}
