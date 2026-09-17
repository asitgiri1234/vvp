package com.example.paymentprocessor.dto;

import com.example.paymentprocessor.entity.TransactionStatus;

import java.math.BigDecimal;

public class TransactionResponse {

    private Long transactionId;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String idempotencyKey;
    private TransactionStatus status;

    public TransactionResponse() {
    }

    public TransactionResponse(
            Long transactionId,
            String senderId,
            String receiverId,
            BigDecimal amount,
            String idempotencyKey,
            TransactionStatus status) {
        this.transactionId = transactionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}
