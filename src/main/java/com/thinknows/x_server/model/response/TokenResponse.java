package com.thinknows.x_server.model.response;

import java.time.LocalDateTime;

public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private LocalDateTime accessTokenExpiry;
    private LocalDateTime refreshTokenExpiry;
    private String tokenType;

    public TokenResponse() {
        this.tokenType = "Bearer";
    }

    public TokenResponse(String accessToken, String refreshToken, 
                         LocalDateTime accessTokenExpiry, LocalDateTime refreshTokenExpiry) {
        this();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public void setAccessTokenExpiry(LocalDateTime accessTokenExpiry) {
        this.accessTokenExpiry = accessTokenExpiry;
    }

    public LocalDateTime getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public void setRefreshTokenExpiry(LocalDateTime refreshTokenExpiry) {
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
