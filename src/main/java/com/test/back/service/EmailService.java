package com.test.back.service;

import com.test.back.dto.ContactRequest;

public interface EmailService {

    void sendContactEmail(ContactRequest request);
}

