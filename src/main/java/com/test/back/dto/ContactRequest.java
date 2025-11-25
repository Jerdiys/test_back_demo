package com.test.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for contact form submissions.
 */
public record ContactRequest(
        @NotBlank(message = "Full name is required.")
        String fullName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be a valid address.")
        String email,

        @NotBlank(message = "Subject is required.")
        String subject,

        @NotBlank(message = "Message is required.")
        @Size(min = 20, message = "Message must be at least 20 characters long.")
        String message
) {
}

