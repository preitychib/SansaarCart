package com.preeti.sansarcart.service;

import com.preeti.sansarcart.entity.Token;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.enums.TokenType;
import com.preeti.sansarcart.exception.custom.TokenException;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.function.Consumer;


@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    @Value("${app.activation.token.expiry-minutes:180}")
    private int activationTokenExpiryMinutes;
    @Value("${app.activation.token.expiry-minutes:15}")
    private int passwordResetTokenExpiryMinutes;


    private String generateToken() {
        byte[] random = new byte[48];
        new SecureRandom().nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }


    public Token createToken(User user, TokenType tokenType){
        // only one token can exist per user
        tokenRepository.deleteByUser(user);
        int expiryMinutes = switch (tokenType) {
            case ACTIVATION -> activationTokenExpiryMinutes;
            case PASSWORD_RESET -> passwordResetTokenExpiryMinutes;
        };
        Token token = Token.builder()
                .token(generateToken())
                .user(user)
                .expiry(Instant.now().plus(Duration.ofMinutes(expiryMinutes)))
                .type(tokenType)
                .build();
        return tokenRepository.save(token);
    }


    public void handleTokenUse(String tokenValue, Consumer<User> onValidAction,Consumer<User> onTokenExpiry, TokenType tokenType) {
        Token token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ValidationException("Token is invalid"));

        if (token.getExpiry().isBefore(Instant.now())) {
            tokenRepository.delete(token);
            onTokenExpiry.accept(token.getUser());
            throw new TokenException("Token has expired!");
        }
        onValidAction.accept(token.getUser());
        tokenRepository.delete(token);
    }
}
