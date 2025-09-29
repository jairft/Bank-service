package com.accountservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.accountservice.model.PixKey.PixKeyType;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pix_transactions")
public class PixTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false, length = 50)
    private String transactionId;
    
    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;
    
    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;
    
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;
    
    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PixKeyType keyType;
    
    @Column(name = "pix_key", nullable = false, length = 100)
    private String pixKey;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    public enum TransactionStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, REVERSED
    }

    public PixTransaction(Long id, String transactionId, Long fromUserId, Long fromAccountId, Long toUserId,
            Long toAccountId, BigDecimal amount, PixKeyType keyType, String pixKey, String description,
            TransactionStatus status, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.fromUserId = fromUserId;
        this.fromAccountId = fromAccountId;
        this.toUserId = toUserId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.keyType = keyType;
        this.pixKey = pixKey;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public PixTransaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PixKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(PixKeyType keyType) {
        this.keyType = keyType;
    }

    public String getPixKey() {
        return pixKey;
    }

    public void setPixKey(String pixKey) {
        this.pixKey = pixKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    
}