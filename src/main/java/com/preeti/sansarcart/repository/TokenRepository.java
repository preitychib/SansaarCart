package com.preeti.sansarcart.repository;

import com.preeti.sansarcart.entity.Token;
import com.preeti.sansarcart.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {

    @Transactional
    void deleteByUser(User user);

    void deleteByExpiryBefore(Instant time);
    Optional<Token> findByToken(String token);
}
