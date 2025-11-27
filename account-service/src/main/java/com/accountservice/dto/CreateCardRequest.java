package com.accountservice.dto;

import java.math.BigDecimal;

import com.accountservice.model.CardBrand;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class CreateCardRequest {

    @NotNull
    private CardBrand brand;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal requestedLimit;

    public CardBrand getBrand() {
        return brand;
    }

    public void setBrand(CardBrand brand) {
        this.brand = brand;
    }

    public BigDecimal getRequestedLimit() {
        return requestedLimit;
    }

    public void setRequestedLimit(BigDecimal requestedLimit) {
        this.requestedLimit = requestedLimit;
    }

    

}