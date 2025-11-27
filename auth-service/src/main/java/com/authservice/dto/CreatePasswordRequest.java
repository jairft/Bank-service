package com.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePasswordRequest {

    @NotBlank(message = "Token é obrigatório")
    private String token;

    @NotBlank(message = "Nova senha de avesso é obrigatória")
    @Size(min = 6, message = "Senha de acesso deve ter no mínimo 6 caracteres")
    private String newPassword;

    

    /**
     * @return String return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return String return the newPassword
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @param newPassword the newPassword to set
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
