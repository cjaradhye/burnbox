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
    
    @Value("${app.frontend.url:https://burnbox-spark.vercel.app}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        if (userService == null || jwtService == null) {
            log.error("Required services not available for OAuth2 authentication");
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
            
            // Build redirect URL with all user information
            StringBuilder redirectUrl = new StringBuilder(frontendUrl)
                    .append("/auth/callback")
                    .append("?token=").append(URLEncoder.encode(jwt, StandardCharsets.UTF_8))
                    .append("&userId=").append(URLEncoder.encode(user.getUserId(), StandardCharsets.UTF_8))
                    .append("&name=").append(URLEncoder.encode(name != null ? name : "", StandardCharsets.UTF_8))
                    .append("&email=").append(URLEncoder.encode(email != null ? email : "", StandardCharsets.UTF_8))
                    .append("&picture=").append(URLEncoder.encode(picture != null ? picture : "", StandardCharsets.UTF_8));
            
            log.info("OAuth2 authentication successful for user: {} (ID: {})", email, user.getUserId());
            response.sendRedirect(redirectUrl.toString());
            
        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication", e);
            response.sendRedirect(frontendUrl + "/auth/error");
        }
    }
}
