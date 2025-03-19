package com.thinknows.x_server.controller;

import com.thinknows.x_server.model.User;
import com.thinknows.x_server.model.request.LoginRequest;
import com.thinknows.x_server.model.request.RefreshTokenRequest;
import com.thinknows.x_server.model.request.RegisterRequest;
import com.thinknows.x_server.model.response.ApiResponse;
import com.thinknows.x_server.model.response.LoginResponse;
import com.thinknows.x_server.model.response.TokenResponse;
import com.thinknows.x_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterRequest request) {
        // Validate request
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Username is required"));
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Password is required"));
        }

        // Register user
        User user = userService.register(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Username already exists"));
        }

        // Create a copy of the user without password for the response
        User userResponse = new User();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        userResponse.setActive(user.isActive());
        
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", userResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        // Validate request
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Username is required"));
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Password is required"));
        }

        try {
            // Login user
            User user = userService.login(request);
            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid username or password"));
            }

            // Generate tokens
            TokenResponse tokens = userService.generateTokens(user);
            
            // Create a clean user object for response
            User userResponse = new User();
            userResponse.setId(user.getId());
            userResponse.setUsername(user.getUsername());
            userResponse.setEmail(user.getEmail());
            userResponse.setPhone(user.getPhone());
            userResponse.setCreatedAt(user.getCreatedAt());
            userResponse.setUpdatedAt(user.getUpdatedAt());
            userResponse.setActive(user.isActive());
            
            LoginResponse loginResponse = new LoginResponse(tokens, userResponse);
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Internal server error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        // Validate request
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Refresh token is required"));
        }
        
        try {
            // Refresh token
            TokenResponse tokens = userService.refreshToken(request);
            if (tokens == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid or expired refresh token"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", tokens));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Internal server error: " + e.getMessage()));
        }
    }
}
