package com.disposablemailservice.controller;

import com.disposablemailservice.service.EmailEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sns")
public class SnsEventController {
    private final EmailEventService emailEventService;

    public SnsEventController(EmailEventService emailEventService) {
        this.emailEventService = emailEventService;
    }

    @PostMapping("/event")
    public ResponseEntity<?> handleSnsEvent(@RequestBody String snsPayload) {
        emailEventService.processSnsEvent(snsPayload);
        return ResponseEntity.ok(Map.of("status", "processed"));
    }
}
