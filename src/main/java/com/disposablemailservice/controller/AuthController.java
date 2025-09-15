package com.disposablemailservice.controller;

import com.disposablemailservice.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @GetMapping("/google/login")
    public RedirectView googleLogin() {
        // This will redirect to Google OAuth2 login
        return new RedirectView("/oauth2/authorization/google");
    }
    
    @GetMapping("/google/error")
    public RedirectView googleError() {
        return new RedirectView("http://localhost:3000/auth/error?message=Authentication failed");
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        return ResponseEntity.ok(Map.of(
            "id", user.getUserId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "picture", user.getPicture()
        ));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Since we're using stateless JWT, logout is handled client-side
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}