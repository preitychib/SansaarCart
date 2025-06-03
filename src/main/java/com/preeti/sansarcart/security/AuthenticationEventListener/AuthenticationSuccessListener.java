package com.preeti.sansarcart.security.AuthenticationEventListener;

import com.preeti.sansarcart.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserRepository userRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();

        userRepository.findByEmail(username).ifPresent(user -> {
            if (user.getInvalidAttemptCount() > 0 || user.isAccountLocked()) {
                user.setInvalidAttemptCount(0);
                user.setAccountLocked(false);
                user.setLockedAt(null);
                userRepository.save(user);
            }
        });
    }
}
