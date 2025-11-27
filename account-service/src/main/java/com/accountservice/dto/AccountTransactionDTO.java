package com.accountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AccountTransactionDTO {

    private String transactionId;      // ID da transação
    private String type;               // "DEPOSIT" ou "PIX"
    private String direction;          // "IN" ou "OUT"
    private BigDecimal amount;         // Valor da transação
    private String description;        // Descrição
    private String pixKey;  
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")           // Chave PIX (se for PIX)
    private LocalDateTime createdAt;   // Data/hora da transação
    private String fromUserName;       // Nome do remetente
    private String toUserName;         // Nome do destinatário

    // ===== Getters e Setters =====
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
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
    public String getPixKey() {
        return pixKey;
    }
    public void setPixKey(String pixKey) {
        this.pixKey = pixKey;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getFromUserName() {
        return fromUserName;
    }
    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }
    public String getToUserName() {
        return toUserName;
    }
    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
}
