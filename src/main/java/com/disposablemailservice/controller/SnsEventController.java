package com.disposablemailservice.controller;

import com.disposablemailservice.service.EmailEventService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sns")
@Profile("aws")
public class SnsEventController {
    private final EmailEventService emailEventService;
    private final ObjectMapper objectMapper;

    public SnsEventController(EmailEventService emailEventService) {
        this.emailEventService = emailEventService;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/event")
    public ResponseEntity<?> handleSnsEvent(@RequestBody String snsPayload) {
        try {
            System.out.println("Received SNS payload: " + snsPayload);
            
            // Parse JSON properly
            JsonNode snsMessage = objectMapper.readTree(snsPayload);
            String messageType = snsMessage.get("Type").asText();
            
            if ("SubscriptionConfirmation".equals(messageType)) {
                // Extract SubscribeURL properly
                String subscribeUrl = snsMessage.get("SubscribeURL").asText();
                System.out.println("Found SubscribeURL: " + subscribeUrl);
                
                if (subscribeUrl != null && !subscribeUrl.isEmpty()) {
                    confirmSubscription(subscribeUrl);
                    return ResponseEntity.ok(Map.of("status", "subscription confirmed"));
                }
            } else if ("Notification".equals(messageType)) {
                // Process actual email event notification
                emailEventService.processSnsEvent(snsPayload);
                return ResponseEntity.ok(Map.of("status", "event processed"));
            }
        } catch (Exception e) {
            System.err.println("Error processing SNS event: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(Map.of("status", "received"));
    }
    private void confirmSubscription(String subscribeUrl) {
        try {
            System.out.println("Attempting to confirm subscription with URL: " + subscribeUrl);
            
            // Make HTTP GET request to confirm
            java.net.URI uri = java.net.URI.create(subscribeUrl);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println("SNS subscription confirmed successfully. Response: " + response.statusCode());
            System.out.println("Response body: " + response.body());
        } catch (Exception e) {
            System.err.println("Error confirming subscription: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
