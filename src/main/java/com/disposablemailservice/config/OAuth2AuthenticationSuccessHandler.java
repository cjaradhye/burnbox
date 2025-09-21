package com.disposablemailservice.config;

import com.disposablemailservice.model.User;
import com.disposablemailservice.service.JwtService;
import com.disposablemailservice.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired(required = false)
    private UserService userService;
    
    @Autowired(required = false)
    private JwtService jwtService;
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        if (userService == null || jwtService == null) {
            log.error("Required services not available for OAuth2 authentication");
            String frontendUrl = allowedOrigins.split(",")[0];
            response.sendRedirect(frontendUrl + "/auth/error");
            return;
        }
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        try {
            // Extract user information from OAuth2 response
            String googleId = oauth2User.getAttribute("sub");
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            String picture = oauth2User.getAttribute("picture");
            
            // Create or update user in database
            User user = userService.createOrUpdateUser(googleId, name, email, picture);
            
            // Generate JWT token
            String jwt = jwtService.generateToken(user);
            
            // Get the frontend URL (first allowed origin)
            String frontendUrl = allowedOrigins.split(",")[0];
            
            // Redirect to frontend with JWT token
            String redirectUrl = frontendUrl + "/auth/callback?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
            
            log.info("OAuth2 authentication successful for user: {}", email);
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication", e);
            String frontendUrl = allowedOrigins.split(",")[0];
            response.sendRedirect(frontendUrl + "/auth/error");
        }
    }
}
