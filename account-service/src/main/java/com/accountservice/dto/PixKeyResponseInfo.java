package com.accountservice.dto;

import com.accountservice.model.PixKey;
import com.accountservice.model.PixKey.PixKeyType;

public class PixKeyResponseInfo {

    private String ownerName;
    private PixKey.PixKeyType keyType;
    private String keyValue;
    private String bank;

    public PixKeyResponseInfo(String ownerName, PixKeyType keyType, String keyValue, String bank) {
        this.ownerName = ownerName;
        this.keyType = keyType;
        this.keyValue = keyValue;
        this.bank = bank;
    }

    public PixKeyResponseInfo() {
    }

    public String getOwnerName() {
        return ownerName;
    }

    public PixKeyType getKeyType() {
        return keyType;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public String getBank() {
        return bank;
    }
}

