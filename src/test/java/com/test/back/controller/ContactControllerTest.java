package com.test.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.back.dto.ContactRequest;
import com.test.back.service.ContactService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactService contactService;

    @Test
    @DisplayName("Should accept valid contact request")
    void submitContact_validRequest_returnsAccepted() throws Exception {
        ContactRequest request = new ContactRequest(
                "Jane Doe",
                "jane@example.com",
                "Pricing",
                "I would love to talk about enterprise pricing plans!"
        );
        doNothing().when(contactService).processContactRequest(request);

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("Message accepted and queued for delivery."));
    }

    @Test
    @DisplayName("Should reject invalid contact request payload")
    void submitContact_invalidRequest_returnsBadRequest() throws Exception {
        String invalidPayload = """
                {
                  "fullName": "",
                  "email": "bad-email",
                  "subject": "",
                  "message": "Too short"
                }
                """;

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.subject").exists())
                .andExpect(jsonPath("$.errors.message").exists());
    }
}

