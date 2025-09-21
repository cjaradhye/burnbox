package com.disposablemailservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "message", "Burnbox Backend is running! 🚀",
            "status", "UP",
            "version", "1.0.0"
        );
    }

    @GetMapping("/api")
    public Map<String, String> api() {
        return Map.of(
            "message", "Burnbox API is ready",
            "status", "UP"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "service", "burnbox-backend"
        );
    }
}