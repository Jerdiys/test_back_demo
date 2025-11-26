package com.test.back.service.impl;

import com.test.back.dto.ContactRequest;
import com.test.back.exception.EmailDeliveryException;
import com.test.back.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String targetEmail;
    private final String senderEmail;
    private final String senderName;

    public EmailServiceImpl(
            RestTemplate restTemplate,
            @Value("${brevo.api-key}") String apiKey,
            @Value("${contact.target-email}") String targetEmail,
            @Value("${brevo.sender-email:no-reply@nexuscore.com}") String senderEmail,
            @Value("${brevo.sender-name:Nexus Core Platform}") String senderName) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.targetEmail = targetEmail;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        validateConfiguration();
    }

    @Override
    public void sendContactEmail(ContactRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> emailPayload = Map.of(
                    "sender", Map.of(
                            "name", senderName,
                            "email", senderEmail
                    ),
                    "to", List.of(Map.of(
                            "email", targetEmail
                    )),
                    "subject", String.format("New Inquiry: %s (From: %s)", request.subject(), request.fullName()),
                    "textContent", buildBody(request)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(emailPayload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    BREVO_API_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.info("Contact message from {} ({}) sent successfully to {}", 
                    request.fullName(), request.email(), targetEmail);
        } catch (RestClientException ex) {
            log.error("Failed to send contact message for {} via Brevo API", request.email(), ex);
            throw new EmailDeliveryException("Unable to send email at this time.", ex);
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(apiKey) || apiKey.equals("your-api-key-here")) {
            throw new IllegalStateException("BREVO_API_KEY must be provided.");
        }
        if (!StringUtils.hasText(targetEmail)) {
            throw new IllegalStateException("CONTACT_TARGET_EMAIL must be provided.");
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

