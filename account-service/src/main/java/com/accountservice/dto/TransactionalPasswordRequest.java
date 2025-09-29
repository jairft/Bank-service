package com.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class TransactionalPasswordRequest {
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 6, message = "Senha deve ter exatamente 6 dígitos")
    @Pattern(regexp = "\\d{6}", message = "Senha deve conter apenas números")
    private String password;
    
    private String confirmPassword; // Para criação

    

    public TransactionalPasswordRequest(
            @NotBlank(message = "Senha é obrigatória") @Size(min = 6, max = 6, message = "Senha deve ter exatamente 6 dígitos") @Pattern(regexp = "\\d{6}", message = "Senha deve conter apenas números") String password,
            String confirmPassword) {
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    

    public TransactionalPasswordRequest() {
    }



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    
}