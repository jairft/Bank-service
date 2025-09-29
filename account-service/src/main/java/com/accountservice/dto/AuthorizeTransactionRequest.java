package com.accountservice.dto;

import jakarta.validation.constraints.NotBlank;


public class AuthorizeTransactionRequest {
    
    @NotBlank(message = "Senha transacional é obrigatória")
    private String transactionalPassword;

    public String getTransactionalPassword() {
        return transactionalPassword;
    }

    public void setTransactionalPassword(String transactionalPassword) {
        this.transactionalPassword = transactionalPassword;
    }

    
}