package com.authservice.dto;

import lombok.Data;

@Data
public class ActivationResponse {
    private String message;
    private String email;
    private boolean success;

    public ActivationResponse(String message, String email, boolean success) {
        this.message = message;
        this.email = email;
        this.success = success;
    }
}
