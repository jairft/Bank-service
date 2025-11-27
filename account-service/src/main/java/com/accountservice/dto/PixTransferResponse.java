package com.accountservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.accountservice.model.PixTransaction;
import com.accountservice.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;


public class PixTransferResponse {
    private String transactionId;
    private String status;
    private BigDecimal amount;
    private TransactionType type;
    private String fromAccount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    
    public PixTransferResponse(PixTransaction transaction, String message) {
        this.transactionId = transaction.getTransactionId();
        this.status = transaction.getStatus().toString();
        this.amount = transaction.getAmount();
        this.timestamp = transaction.getCreatedAt();
        this.message = message;
    }

    

    public PixTransferResponse() {
    }



    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



    public TransactionType getType() {
        return type;
    }



    public void setType(TransactionType type) {
        this.type = type;
    }

    
}