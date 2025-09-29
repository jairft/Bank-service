package com.accountservice.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "pix_keys", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"key_value", "key_type"})
})
public class PixKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 20)
    private PixKeyType keyType;
    
    @Column(name = "key_value", nullable = false, length = 100)
    private String keyValue;
    
    @Column(nullable = false, length = 100)
    private String ownerName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PixKeyStatus status = PixKeyStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum PixKeyType {
        CPF, EMAIL, TELEFONE, RANDOM
    }
    
    public enum PixKeyStatus {
        ACTIVE, INACTIVE, BLOCKED
    }

    public PixKey(Long id, Long userId, PixKeyType keyType, String keyValue, String ownerName, PixKeyStatus status,
            LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.keyType = keyType;
        this.keyValue = keyValue;
        this.ownerName = ownerName;
        this.status = status;
        this.createdAt = createdAt;
    }

    public PixKey() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public PixKeyStatus getStatus() {
        return status;
    }

    public void setStatus(PixKeyStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    
}