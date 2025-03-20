package com.thinknows.x_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

import com.thinknows.x_server.model.AppConfig;

@RestController
@RequestMapping("/api/v1")
public class ConfigController {

    @GetMapping("/config")
    public ResponseEntity<AppConfig> config() {
        // Create a new configuration object
        AppConfig config = new AppConfig();
        
        // Set basic configuration
        config.setAppVersion("1.0.0");
        config.setApiBaseUrl("/api/v1");
        config.setDebugMode(false);
        
        // Add API endpoints
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("login", "/api/v1/user/login");
        endpoints.put("register", "/api/v1/user/register");
        endpoints.put("refresh", "/api/v1/user/refresh");
        endpoints.put("profile", "/api/v1/user/profile");
        endpoints.put("settings", "/api/v1/user/settings");
        config.setEndpoints(endpoints);
        
        // Add feature flags
        Map<String, Object> features = new HashMap<>();
        features.put("darkMode", true);
        features.put("notifications", true);
        features.put("maxUploadSize", 10485760); // 10MB in bytes
        features.put("allowedFileTypes", new String[]{"jpg", "png", "pdf", "doc", "docx"});
        config.setFeatures(features);
        
        return ResponseEntity.ok(config);
    }
}
