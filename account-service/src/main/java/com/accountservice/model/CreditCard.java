package com.accountservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_cards")
public class CreditCard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private CardBrand brand;

    @Column(length = 32)
    private String cardNumber; // armazenar o número completo (experiência)

    private String cardNumberMasked;

    private String expiry; // MM/yy

    private String cvv; // em texto claro (atenção)

    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;

    private BigDecimal approvedLimit;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public CardBrand getBrand() { return brand; }
    public void setBrand(CardBrand brand) { this.brand = brand; }


    public String getCardNumberMasked() { return cardNumberMasked; }
    public void setCardNumberMasked(String cardNumberMasked) { this.cardNumberMasked = cardNumberMasked; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public BigDecimal getApprovedLimit() { return approvedLimit; }
    public void setApprovedLimit(BigDecimal approvedLimit) { this.approvedLimit = approvedLimit; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    public String getCvv() {
        return cvv;
    }
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    public CardStatus getStatus() {
        return status;
    }
    public void setStatus(CardStatus status) {
        this.status = status;
    }

    
}


