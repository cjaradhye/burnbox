package com.disposablemailservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for Render monitoring
 * Provides simple health endpoints for deployment platforms
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * Simple health check endpoint for Render
     * Returns 200 OK if the application is running
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        status.put("application", "Burnbox Backend");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(status);
    }

    /**
     * Database health check
     * Returns 200 OK if database connection is working
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, String>> dbHealth() {
        Map<String, String> status = new HashMap<>();
        
        if (dataSource == null) {
            status.put("status", "DOWN");
            status.put("error", "DataSource not configured");
            return ResponseEntity.status(503).body(status);
        }
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                status.put("status", "UP");
                status.put("database", "PostgreSQL");
                status.put("url", connection.getMetaData().getURL());
                return ResponseEntity.ok(status);
            } else {
                status.put("status", "DOWN");
                status.put("error", "Database connection invalid");
                return ResponseEntity.status(503).body(status);
            }
        } catch (SQLException e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            return ResponseEntity.status(503).body(status);
        }
    }

    /**
     * Readiness probe for Kubernetes/container orchestration
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "READY");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(status);
    }

    /**
     * Liveness probe for Kubernetes/container orchestration
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> live() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ALIVE");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(status);
    }
}