package com.disposablemailservice.repository;

import com.disposablemailservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUserId(String userId);
    
    Optional<User> findByGoogleId(String googleId);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUserId(String userId);
    
    boolean existsByGoogleId(String googleId);
    
    boolean existsByEmail(String email);
}
