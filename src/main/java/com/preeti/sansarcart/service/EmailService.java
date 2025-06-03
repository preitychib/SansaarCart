package com.preeti.sansarcart.service;

import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.payload.email.EmailDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;


    @Async
    public void sendEmail(EmailDetails emailDetails) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(emailDetails.getTo());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getText(), true);
        } catch (MessagingException e) {
            throw new RuntimeException("Mail service error "+e.getMessage());
        }
        emailSender.send(message);
    }
}
