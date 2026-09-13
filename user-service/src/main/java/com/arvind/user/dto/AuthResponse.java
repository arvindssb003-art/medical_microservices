package com.arvind.user.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long refreshExpiresIn;
    private String tokenType;

    public AuthResponse() {
    }

    public AuthResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            long refreshExpiresIn,
            String tokenType) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.tokenType = tokenType;
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
}