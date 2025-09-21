package com.disposablemailservice.config;

import com.disposablemailservice.model.User;
import com.disposablemailservice.service.JwtService;
import com.disposablemailservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private final JwtService jwtService;
    private final UserService userService;
    
    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        log.info("🔐 [AUTH STEP 1] OAuth2 authentication success callback triggered");
        log.info("🌐 [AUTH STEP 2] Request details:");
        log.info("   🔗 Request URI: {}", request.getRequestURI());
        log.info("   🎯 Remote Address: {}", request.getRemoteAddr());
        log.info("   📊 User Agent: {}", request.getHeader("User-Agent"));
        
        try {
            log.info("👤 [AUTH STEP 3] Processing authentication principal...");
            log.info("   🔍 Principal type: {}", authentication.getPrincipal().getClass().getSimpleName());
            
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                log.info("✅ [AUTH STEP 4] OAuth2User principal confirmed");
                
                log.info("📋 [AUTH STEP 5] Extracting user attributes from Google OAuth2:");
                // Extract user info from Google OAuth2
                String googleId = oauth2User.getAttribute("sub");
                String name = oauth2User.getAttribute("name");
                String email = oauth2User.getAttribute("email");
                String picture = oauth2User.getAttribute("picture");
                
                log.info("   🆔 Google ID: {}", googleId);
                log.info("   👤 Name: {}", name);
                log.info("   📧 Email: {}", email);
                log.info("   🖼️ Picture URL: {}", picture);
                
                // Validate required fields
                log.info("🔍 [AUTH STEP 6] Validating required fields...");
                if (googleId == null || email == null) {
                    log.error("❌ [AUTH STEP 7] Missing required user information:");
                    log.error("   🆔 Google ID: {}", googleId);
                    log.error("   📧 Email: {}", email);
                    redirectToError(response, "Missing required user information from Google");
                    return;
                }
                log.info("✅ [AUTH STEP 7] Required fields validation passed");
                
                // Save or update user in database
                log.info("💾 [AUTH STEP 8] Creating or updating user in database...");
                User user = userService.createOrUpdateUser(googleId, name, email, picture);
                log.info("✅ [AUTH STEP 9] User processed successfully:");
                log.info("   🆔 User ID: {}", user.getUserId());
                log.info("   👤 Name: {}", user.getName());
                log.info("   📧 Email: {}", user.getEmail());
                log.info("   🖼️ Picture: {}", user.getPicture());
                
                // Generate JWT token
                log.info("🔑 [AUTH STEP 10] Generating JWT token...");
                String jwt = jwtService.generateToken(user);
                log.info("✅ [AUTH STEP 11] JWT token generated successfully (length: {})", jwt.length());
                
                // Redirect to frontend with token and user data
                log.info("🔄 [AUTH STEP 12] Preparing frontend redirect URL...");
                String frontendUrl = String.format(
                    "http://localhost:3000/auth/callback?token=%s&userId=%s&name=%s&email=%s&picture=%s",
                    jwt,
                    user.getUserId(),
                    URLEncoder.encode(user.getName() != null ? user.getName() : "", StandardCharsets.UTF_8),
                    URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8),
                    URLEncoder.encode(user.getPicture() != null ? user.getPicture() : "", StandardCharsets.UTF_8)
                );
                
                log.info("🎯 [AUTH STEP 13] Frontend redirect URL prepared:");
                log.info("   🔗 URL: {}", frontendUrl.substring(0, Math.min(frontendUrl.length(), 100)) + "...");
                
                log.info("🚀 [AUTH STEP 14] Performing redirect to frontend...");
                getRedirectStrategy().sendRedirect(request, response, frontendUrl);
                log.info("🎉 [AUTH STEP 15] OAuth2 authentication flow completed successfully!");
                
            } else {
                log.error("❌ [AUTH STEP 4] Invalid principal type: {}", 
                         authentication.getPrincipal().getClass().getName());
                redirectToError(response, "Invalid authentication principal type");
            }
            
        } catch (Exception e) {
            log.error("💥 [AUTH ERROR] Exception during authentication processing: {}", e.getMessage());
            log.error("📍 [AUTH ERROR] Stack trace:", e);
            redirectToError(response, "Authentication processing failed: " + e.getMessage());
        }
    }
    
    private void redirectToError(HttpServletResponse response, String message) throws IOException {
        log.error("⚠️ [AUTH ERROR] Redirecting to error page with message: {}", message);
        String errorUrl = "http://localhost:3000/auth/error?message=" + 
                         URLEncoder.encode(message, StandardCharsets.UTF_8);
        log.info("🔗 [AUTH ERROR] Error redirect URL: {}", errorUrl);
        response.sendRedirect(errorUrl);
        log.info("🔄 [AUTH ERROR] Error redirect completed");
    }
}