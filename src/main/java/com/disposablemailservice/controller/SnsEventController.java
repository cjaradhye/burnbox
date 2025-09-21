package com.disposablemailservice.controller;

import com.disposablemailservice.service.EmailEventService;
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

    public SnsEventController(EmailEventService emailEventService) {
        this.emailEventService = emailEventService;
    }

    @PostMapping("/event")
    public ResponseEntity<?> handleSnsEvent(@RequestBody String snsPayload) {
        try {
            // Parse JSON to check message type
            if (snsPayload.contains("\"Type\" : \"SubscriptionConfirmation\"")) {
                // Extract SubscribeURL and confirm
                String subscribeUrl = extractSubscribeUrl(snsPayload);
                if (subscribeUrl != null) {
                    // Make HTTP GET request to confirm subscription
                    confirmSubscription(subscribeUrl);
                    return ResponseEntity.ok(Map.of("status", "subscription confirmed"));
                }
            } else if (snsPayload.contains("\"Type\" : \"Notification\"")) {
                // Process actual email event notification
                emailEventService.processSnsEvent(snsPayload);
                return ResponseEntity.ok(Map.of("status", "event processed"));
            }
        } catch (Exception e) {
            System.err.println("Error processing SNS event: " + e.getMessage());
        }
        
        return ResponseEntity.ok(Map.of("status", "received"));
    }
    
    private String extractSubscribeUrl(String snsPayload) {
        // Simple extraction - in production, use proper JSON parsing
        try {
            String[] lines = snsPayload.split("\n");
            for (String line : lines) {
                if (line.contains("SubscribeURL")) {
                    return line.split("\"")[3];
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting subscribe URL: " + e.getMessage());
        }
        return null;
    }
    
    private void confirmSubscription(String subscribeUrl) {
        try {
            // Make HTTP GET request to confirm
            java.net.URI uri = java.net.URI.create(subscribeUrl);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println("SNS subscription confirmed successfully");
        } catch (Exception e) {
            System.err.println("Error confirming subscription: " + e.getMessage());
        }
    }
}
