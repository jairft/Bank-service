package com.accountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateCardResponse {
    private Long cardId;
    private String brand;
    private String cardNumberMasked;
    private String cardNumberFull;
    private String expiry;
    private String cvv;
    private BigDecimal approvedLimit;
    private LocalDateTime createdAt;

    

    
    public CreateCardResponse() {
    }

    public CreateCardResponse(Long cardId, String brand, String cardNumberMasked, String cardNumberFull, String expiry,
            String cvv, BigDecimal approvedLimit, LocalDateTime createdAt) {
        this.cardId = cardId;
        this.brand = brand;
        this.cardNumberMasked = cardNumberMasked;
        this.cardNumberFull = cardNumberFull;
        this.expiry = expiry;
        this.cvv = cvv;
        this.approvedLimit = approvedLimit;
        this.createdAt = createdAt;
    }

    public Long getCardId() {
        return cardId;
    }
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getCardNumberMasked() {
        return cardNumberMasked;
    }
    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }
    public String getCardNumberFull() {
        return cardNumberFull;
    }
    public void setCardNumberFull(String cardNumberFull) {
        this.cardNumberFull = cardNumberFull;
    }
    public String getExpiry() {
        return expiry;
    }
    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
    public String getCvv() {
        return cvv;
    }
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    public BigDecimal getApprovedLimit() {
        return approvedLimit;
    }
    public void setApprovedLimit(BigDecimal approvedLimit) {
        this.approvedLimit = approvedLimit;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    
    // getters/setters, construtores
}