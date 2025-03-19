package com.thinknows.x_server.model.response;

import com.thinknows.x_server.model.User;

public class LoginResponse {
    private TokenResponse tokens;
    private User user;

    public LoginResponse() {
    }

    public LoginResponse(TokenResponse tokens, User user) {
        this.tokens = tokens;
        this.user = user;
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
}
