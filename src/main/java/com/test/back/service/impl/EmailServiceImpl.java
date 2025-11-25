package com.test.back.service.impl;

import com.test.back.dto.ContactRequest;
import com.test.back.exception.EmailDeliveryException;
import com.test.back.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String targetEmail;
    private final String senderEmail;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${contact.target-email}") String targetEmail,
            @Value("${spring.mail.username}") String senderEmail) {
        this.mailSender = mailSender;
        this.targetEmail = targetEmail;
        this.senderEmail = "seanjed82@gmail.com";
        validateConfiguration();
    }

    @Override
    public void sendContactEmail(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(targetEmail);
        message.setFrom(senderEmail);
        message.setSubject(String.format("New Inquiry: %s (From: %s)", request.subject(), request.fullName()));
        message.setText(buildBody(request));
        try {
            mailSender.send(message);
            log.info("Contact message from {} ({}) queued for {}", request.fullName(), request.email(), targetEmail);
        } catch (MailException ex) {
            log.error("Failed to send contact message for {}", request.email(), ex);
            throw new EmailDeliveryException("Unable to send email at this time.", ex);
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(targetEmail)) {
            throw new IllegalStateException("CONTACT_TARGET_EMAIL must be provided.");
        }
        if (!StringUtils.hasText(senderEmail)) {
            throw new IllegalStateException("SPRING_MAIL_USERNAME must be provided.");
        }
    }

    private String buildBody(ContactRequest request) {
        return """
                New Contact Inquiry from Nexus Core Platform:

                Name: %s
                Email: %s
                Subject: %s
                Message:
                %s
                """.formatted(
                request.fullName(),
                request.email(),
                request.subject(),
                request.message());
    }
}

