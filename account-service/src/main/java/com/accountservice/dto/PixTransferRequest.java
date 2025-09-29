package com.accountservice.dto;

import java.math.BigDecimal;

import com.accountservice.model.PixKey;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PixTransferRequest {
    
    @NotNull(message = "Tipo da chave é obrigatório")
    private PixKey.PixKeyType keyType;
    
    @NotBlank(message = "Chave PIX é obrigatória")
    private String pixKey;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;
    
    private String description;

    public PixKey.PixKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(PixKey.PixKeyType keyType) {
        this.keyType = keyType;
    }

    public String getPixKey() {
        return pixKey;
    }

    public void setPixKey(String pixKey) {
        this.pixKey = pixKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
}