package com.disposablemailservice.service;

import com.disposablemailservice.model.User;
import com.disposablemailservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createOrUpdateUser(String googleId, String name, String email, String picture) {
        return userRepository.findByGoogleId(googleId)
            .map(existingUser -> {
                // Update existing user with latest info
                existingUser.setName(name);
                existingUser.setEmail(email);
                existingUser.setPicture(picture);
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> {
                // Create new user
                String userId = UUID.randomUUID().toString();
                User newUser = new User(userId, googleId, name, email, picture);
                return userRepository.save(newUser);
            });
    }
    
    public User findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId).orElse(null);
    }
    
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public boolean existsByGoogleId(String googleId) {
        return userRepository.existsByGoogleId(googleId);
    }
    
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}