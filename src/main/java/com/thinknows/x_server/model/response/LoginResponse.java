package com.thinknows.x_server.model.response;

import com.thinknows.x_server.model.User;
import java.util.List;

public class LoginResponse {
    private TokenResponse tokens;
    private User user;
    private boolean requiresTwoFactor = false; // 是否需要二次验证
    private String twoFactorToken; // 二次验证令牌
    private List<DeviceSession> activeDevices; // 当前活跃设备列表

    public LoginResponse() {
    }

    public LoginResponse(TokenResponse tokens, User user) {
        this.tokens = tokens;
        this.user = user;
    }
    
    public LoginResponse(TokenResponse tokens, User user, boolean requiresTwoFactor, 
                        String twoFactorToken, List<DeviceSession> activeDevices) {
        this.tokens = tokens;
        this.user = user;
        this.requiresTwoFactor = requiresTwoFactor;
        this.twoFactorToken = twoFactorToken;
        this.activeDevices = activeDevices;
    }

    public TokenResponse getTokens() {
        return tokens;
    }

    public void setTokens(TokenResponse tokens) {
        this.tokens = tokens;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isRequiresTwoFactor() {
        return requiresTwoFactor;
    }
    
    public void setRequiresTwoFactor(boolean requiresTwoFactor) {
        this.requiresTwoFactor = requiresTwoFactor;
    }
    
    public String getTwoFactorToken() {
        return twoFactorToken;
    }
    
    public void setTwoFactorToken(String twoFactorToken) {
        this.twoFactorToken = twoFactorToken;
    }
    
    public List<DeviceSession> getActiveDevices() {
        return activeDevices;
    }
    
    public void setActiveDevices(List<DeviceSession> activeDevices) {
        this.activeDevices = activeDevices;
    }
}
