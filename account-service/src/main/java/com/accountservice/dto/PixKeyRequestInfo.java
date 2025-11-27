package com.accountservice.dto;

import com.accountservice.model.PixKey.PixKeyType;

public class PixKeyRequestInfo {

    private PixKeyType keyType;
    private String keyValue;

    public PixKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(PixKeyType keyType) {
        this.keyType = keyType;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }
    
}
