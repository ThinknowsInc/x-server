package com.thinknows.x_server.model;

import java.util.Map;
import java.util.HashMap;

public class AppConfig {
    private String appVersion;
    private String apiBaseUrl;
    private boolean debugMode;
    private Map<String, String> endpoints;
    private Map<String, Object> features;

    public AppConfig() {
        this.endpoints = new HashMap<>();
        this.features = new HashMap<>();
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, String> endpoints) {
        this.endpoints = endpoints;
    }

    public Map<String, Object> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, Object> features) {
        this.features = features;
    }
}
