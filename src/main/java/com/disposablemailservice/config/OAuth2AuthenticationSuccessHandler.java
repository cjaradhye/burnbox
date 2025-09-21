package com.disposablemailservice.config;

import com.disposablemailservice.model.User;
import com.disposablemailservice.service.JwtService;
import com.disposablemailservice.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final UserService userService;
    private final JwtService jwtService;
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public OAuth2AuthenticationSuccessHandler(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
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
        response.sendRedirect(redirectUrl);
    }
}
