package com.accountservice.dto;


import com.accountservice.model.PixKey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class PixKeyRequest {
    
    @NotNull(message = "Tipo da chave é obrigatório")
    private PixKey.PixKeyType keyType;
    
    

    public PixKeyRequest(PixKey.PixKeyType keyType,
            @NotBlank(message = "Valor da chave é obrigatório") String keyValue) {
        this.keyType = keyType;
    }

    public PixKeyRequest() {
    }

    public PixKey.PixKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(PixKey.PixKeyType keyType) {
        this.keyType = keyType;
    }
    
}
