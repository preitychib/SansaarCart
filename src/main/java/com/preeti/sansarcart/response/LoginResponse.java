package com.preeti.sansarcart.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String expiresIn;

    public LoginResponse(String token, long expiresInMillis) {
        this.token = token;
        this.expiresIn = (expiresInMillis / 60000) + " minutes";
    }
}