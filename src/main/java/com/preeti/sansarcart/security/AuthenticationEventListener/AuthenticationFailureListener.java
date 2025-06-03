package com.preeti.sansarcart.security.AuthenticationEventListener;

import com.preeti.sansarcart.repository.user.UserRepository;
import com.preeti.sansarcart.security.service.UserAccountLockService;
import com.preeti.sansarcart.service.EmailBuilderService;
import com.preeti.sansarcart.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final UserRepository userRepository;
    private final UserAccountLockService lockTracker;
    private final EmailService emailService;

    @Value("${app.login.max.invalid.attempts}")
    private int MAX_FAILED_ATTEMPTS;


    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();

        userRepository.findByEmail(username).ifPresent(user -> {
            int newAttempts = user.getInvalidAttemptCount() + 1;
            user.setInvalidAttemptCount(newAttempts);

            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                lockTracker.lockUser(username);
                emailService.sendEmail(EmailBuilderService.buildAccountLockedEmail(username));
                log.debug("User {}  account locked due to too many failed attempts",username);
            }
            userRepository.save(user);
        });
    }
}

