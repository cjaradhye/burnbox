// Main entry point for the Spring Boot application.
// Starts the disposable email service backend.
package com.disposablemailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
public class DisposableEmailServiceApplication {

    private static org.springframework.context.ApplicationContext applicationContext;

    public static void main(String[] args) {
        // Set default profile if none specified
        System.setProperty("spring.profiles.default", "dev");
        
        applicationContext = SpringApplication.run(DisposableEmailServiceApplication.class, args);
        
        // Log startup information
        System.out.println("🚀 Burnbox Application Started Successfully!");
        System.out.println("📍 Health Check: http://localhost:8080/actuator/health");
        System.out.println("📍 API Base: http://localhost:8080/api/mailboxes");
    }

    public static org.springframework.context.ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}