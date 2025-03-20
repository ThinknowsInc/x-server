package com.thinknows.x_server.model.request;

import com.thinknows.x_server.model.DeviceInfo;

public class LoginRequest {
    private String username;
    private String password;
    private String loginType = "PASSWORD"; // PASSWORD, EMAIL, PHONE, SOCIAL
    private String captchaToken; // 验证码令牌
    private boolean rememberMe = false; // 记住我功能
    private DeviceInfo deviceInfo; // 设备信息
    private String socialProvider; // 社交媒体提供商
    private String socialToken; // 社交媒体令牌

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getLoginType() {
        return loginType;
    }
    
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
    
    public String getCaptchaToken() {
        return captchaToken;
    }
    
    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
    
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public String getSocialProvider() {
        return socialProvider;
    }
    
    public void setSocialProvider(String socialProvider) {
        this.socialProvider = socialProvider;
    }
    
    public String getSocialToken() {
        return socialToken;
    }
    
    public void setSocialToken(String socialToken) {
        this.socialToken = socialToken;
    }
}
