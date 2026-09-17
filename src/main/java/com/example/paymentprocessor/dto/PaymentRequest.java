package com.example.paymentprocessor.dto;

import java.math.BigDecimal;

public class PaymentRequest {

    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String idempotencyKey;

    public PaymentRequest() {
    }

    public PaymentRequest(
            String senderId,
            String receiverId,
            BigDecimal amount,
            String idempotencyKey) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}