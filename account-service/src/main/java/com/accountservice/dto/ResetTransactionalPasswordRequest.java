package com.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ResetTransactionalPasswordRequest {
    
    @NotBlank
    private String token;

    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "A senha deve ter 4 dígitos numéricos")
    private String newPassword;

    @NotBlank
    private String confirmPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    
}
