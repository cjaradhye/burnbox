package com.disposablemailservice.service;

import org.springframework.stereotype.Service;

@Service
public class EmailEventService {
    public void processSnsEvent(String snsPayload) {
        // TODO: Parse SNS payload, extract SES message, store metadata in DynamoDB, upload attachments to S3
    }
}
