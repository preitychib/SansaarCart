package com.preeti.sansarcart.service;

import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.repository.user.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {
    private final CustomerRepository customerRepository;
    private final UserService userService;
    private final EmailService emailService;


    public boolean activateUserAndSendMail(UUID id){
        User user = userService.getUserById(id);
        if(!user.isActive()){
            userService.activateUserAccount(user);
            emailService.sendEmail(EmailBuilderService.buildAdminActivationEmail(user.getEmail()));
            return true;
        }
        return false;
    }

    public boolean deactivateUserAndSendMail(UUID id){
        User user = userService.getUserById(id);
        if(user.isActive()){
            userService.deactivateUserAccount(user);
            emailService.sendEmail(EmailBuilderService.buildAdminDeactivationEmail(user.getEmail()));
            return true;
        }
        return false;
    }

}
