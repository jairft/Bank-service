package com.authservice.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String nome;

    public LoginResponse(String token, Long userId, String email, String nome) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.nome = nome;
    }
}

