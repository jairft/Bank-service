package com.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String email;
    
    @NotBlank(message = "Token de redefinição é obrigatório")
    private String token;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
    private String newPassword;

    @NotBlank(message = "Confirmação da nova senha é obrigatória")
    private String confirmNewPassword;
}