package com.arvind.user.dto;

import java.util.List;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long refreshExpiresIn;
    private String tokenType;
    private List<String> roles;

    public AuthResponse() {
    }

    public AuthResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            long refreshExpiresIn,
            String tokenType,
            List<String> roles) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.tokenType = tokenType;
        this.roles = List.copyOf(roles);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public List<String> getRoles() {
        return roles;
    }
}