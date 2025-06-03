package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
    Optional<BlacklistedToken> findByToken(String token);
    @Modifying
    @Query("DELETE FROM BlacklistedToken b WHERE b.expiry < :now")
    void deleteExpiredTokens(Instant now);
}
