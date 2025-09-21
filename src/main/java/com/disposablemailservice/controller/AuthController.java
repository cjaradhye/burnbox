package com.disposablemailservice.controller;

import com.disposablemailservice.model.User;
import com.disposablemailservice.service.JwtService;
import com.disposablemailservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final JwtService jwtService;
    private final UserService userService;
    
    public AuthController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String name = loginRequest.get("name");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        // For demo purposes, create or find user by email
        // In production, you'd validate credentials here
        User user = userService.findByEmail(email);
        if (user == null) {
            // Create new user with basic info
            String googleId = "local_" + UUID.randomUUID().toString(); // Temporary ID for non-OAuth users
            user = userService.createOrUpdateUser(googleId, name != null ? name : "User", email, null);
        }
        
        String jwt = jwtService.generateToken(user);
        
        return ResponseEntity.ok(Map.of(
            "token", jwt,
            "user", Map.of(
                "id", user.getUserId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "picture", user.getPicture() != null ? user.getPicture() : ""
            )
        ));
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