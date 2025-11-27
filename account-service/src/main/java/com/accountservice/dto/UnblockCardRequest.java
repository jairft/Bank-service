package com.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UnblockCardRequest {
    @NotBlank
    @Pattern(regexp = "\\d{3}", message = "CVV deve ter 3 dígitos")
    private String cvv;

    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "Senha transacional deve ter 4 dígitos")
    private String transactionalPassword;
    // getters/setters

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getTransactionalPassword() {
        return transactionalPassword;
    }

    public void setTransactionalPassword(String transactionalPassword) {
        this.transactionalPassword = transactionalPassword;
    }

    
}

