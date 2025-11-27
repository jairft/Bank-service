package com.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateTransactionalPasswordRequest {

    @NotBlank(message = "Senha atual é obrigatória")
    @Pattern(regexp = "\\d{4}", message = "Senha atual deve conter 4 dígitos")
    private String currentPassword;

    @NotBlank(message = "Nova senha é obrigatória")
    @Pattern(regexp = "\\d{4}", message = "Nova senha deve conter 4 dígitos")
    private String newPassword;

    @NotBlank(message = "Confirmação da nova senha é obrigatória")
    @Pattern(regexp = "\\d{4}", message = "Confirmação deve conter 4 dígitos")
    private String confirmPassword;

    // Getters e Setters
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
