// Main entry point for the Spring Boot application.
// Starts the disposable email service backend.
package com.disposablemailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DisposableEmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisposableEmailServiceApplication.class, args);
    }
}