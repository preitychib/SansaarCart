package com.preeti.sansarcart.service;

import com.preeti.sansarcart.payload.authentication.LoginDto;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;


    public User authenticate(LoginDto input) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                input.getEmail(),
                input.getPassword()
        )
        );
        User user = userRepository.findByEmail(input.getEmail())
                        .orElseThrow(() -> new ResourceNotFound("User not found"));
        if (!user.isActive()) {
             throw new ValidationException("Account not verified. Please verify your account.");
         }
         
        return user;
    }

    public void activateUserAccount(User user){
        user.setActive(true);
        userRepository.save(user);
    }
    public void deactivateUserAccount(User user){
        user.setActive(false);
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("User with email " + email + " not found"));
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("User with id " + id + " not found"));
    }


    public void updateUserPassword(User user, String password){
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}

