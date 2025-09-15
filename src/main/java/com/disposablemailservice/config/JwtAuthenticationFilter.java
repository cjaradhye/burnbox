package com.disposablemailservice.config;

import com.disposablemailservice.service.JwtService;
import com.disposablemailservice.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        log.info("🔐 [JWT STEP 1] JWT Authentication filter processing request:");
        log.info("   🎯 Request URI: {}", request.getRequestURI());
        log.info("   🔧 Method: {}", request.getMethod());
        log.info("   🌐 Remote Address: {}", request.getRemoteAddr());
        
        if (request.getServletPath().contains("/auth")) {
            log.info("🚪 [JWT STEP 2] Auth path detected, skipping JWT authentication");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🔍 [JWT STEP 3] Extracting Authorization header...");
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String googleId;
        
        log.info("🔑 [JWT STEP 4] Authorization header: {}", 
                authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 20)) + "..." : "null");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("❌ [JWT STEP 5] No valid Authorization header found, proceeding without authentication");
            filterChain.doFilter(request, response);
            return;
        }
        
        log.info("✅ [JWT STEP 5] Valid Bearer token found");
        jwt = authHeader.substring(7);
        log.info("🎫 [JWT STEP 6] Extracted JWT token (length: {})", jwt.length());
        
        try {
            log.info("🔍 [JWT STEP 7] Extracting username from JWT token...");
            googleId = jwtService.extractUsername(jwt);
            log.info("👤 [JWT STEP 8] Extracted Google ID: {}", googleId);
            
            if (googleId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("🔍 [JWT STEP 9] Google ID found and no existing authentication, looking up user...");
                var user = this.userService.findByGoogleId(googleId);
                
                if (user != null) {
                    log.info("✅ [JWT STEP 10] User found in database:");
                    log.info("   👤 User ID: {}", user.getUserId());
                    log.info("   📧 Email: {}", user.getEmail());
                    log.info("   📛 Name: {}", user.getName());
                    
                    log.info("🔐 [JWT STEP 11] Validating JWT token...");
                    if (jwtService.isTokenValid(jwt, googleId)) {
                        log.info("✅ [JWT STEP 12] JWT token is valid, creating authentication token");
                        
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                null
                        );
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        
                        log.info("🔒 [JWT STEP 13] Setting authentication in SecurityContext");
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("🎉 [JWT STEP 14] JWT authentication completed successfully for user: {}", user.getUserId());
                    } else {
                        log.warn("❌ [JWT STEP 12] JWT token validation failed for user: {}", googleId);
                    }
                } else {
                    log.warn("❌ [JWT STEP 10] User not found in database for Google ID: {}", googleId);
                }
            } else if (googleId == null) {
                log.warn("❌ [JWT STEP 9] Could not extract Google ID from JWT token");
            } else {
                log.info("ℹ️ [JWT STEP 9] User already authenticated, skipping JWT processing");
            }
        } catch (Exception e) {
            log.error("💥 [JWT ERROR] Exception during JWT processing: {}", e.getMessage());
            log.error("📍 [JWT ERROR] Stack trace:", e);
        }
        
        log.info("➡️ [JWT STEP 15] Proceeding with filter chain");
        filterChain.doFilter(request, response);
    }
}