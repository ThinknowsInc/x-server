package com.thinknows.x_server.model.request;

public class SocialLoginRequest {
    private String provider; // 社交媒体提供商: GOOGLE, FACEBOOK, TWITTER, WECHAT, WEIBO 等
    private String token;    // 社交媒体访问令牌
    private String deviceInfo;
    private boolean rememberMe = false;

    public SocialLoginRequest() {
    }

    public SocialLoginRequest(String provider, String token) {
        this.provider = provider;
        this.token = token;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
