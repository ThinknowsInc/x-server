package com.thinknows.x_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thinknows.x_server.model.AppConfig;
import com.thinknows.x_server.model.Endpoint;
import com.thinknows.x_server.model.response.ApiResponse;

@RestController
@RequestMapping("/api/v1")
public class ConfigController {

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<AppConfig>> config() {
        // Create a new configuration object
        AppConfig config = new AppConfig();
        
        // Set basic configuration
        config.setAppVersion("1.0.0");
        config.setApiBaseUrl("/api/v1");
        config.setDebugMode(false);
        
        // Add API endpoints as a list of Endpoint objects
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("login", "/api/v1/user/login"));
        endpoints.add(new Endpoint("register", "/api/v1/user/register"));
        endpoints.add(new Endpoint("refresh", "/api/v1/user/refresh"));
        endpoints.add(new Endpoint("profile", "/api/v1/user/profile"));
        endpoints.add(new Endpoint("settings", "/api/v1/user/settings"));
        config.setEndpoints(endpoints);
        
        // Add feature flags
        Map<String, Object> features = new HashMap<>();
        features.put("darkMode", true);
        features.put("notifications", true);
        features.put("maxUploadSize", 10485760); // 10MB in bytes
        features.put("allowedFileTypes", new String[]{"jpg", "png", "pdf", "doc", "docx"});
        config.setFeatures(features);
        
        // Return with ApiResponse format including code, message, and data
        return ResponseEntity.ok(new ApiResponse<>(200, "Configuration retrieved successfully", config));
    }
}
