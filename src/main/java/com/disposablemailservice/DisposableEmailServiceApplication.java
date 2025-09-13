// Main entry point for the Spring Boot application.
// Starts the disposable email service backend.
package com.disposablemailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DisposableEmailServiceApplication {

    private static org.springframework.context.ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(DisposableEmailServiceApplication.class, args);
    }

    public static org.springframework.context.ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}