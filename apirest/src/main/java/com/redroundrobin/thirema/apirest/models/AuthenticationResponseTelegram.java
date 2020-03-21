package com.redroundrobin.thirema.apirest.models;

public class AuthenticationResponseTelegram {

    private final int code;
    private final String token;

    public AuthenticationResponseTelegram(int code, String token) {
        this.code = code;
        this.token = token;
    }

    public int getCode() {
        return code;
    }

    public String getToken() {
        return token;
    }
}
