package com.test.back.service;

import com.test.back.dto.ContactRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final EmailService emailService;

    public ContactService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void processContactRequest(ContactRequest request) {
        log.debug("Processing contact request for {}", request.email());
        emailService.sendContactEmail(request);
    }
}

