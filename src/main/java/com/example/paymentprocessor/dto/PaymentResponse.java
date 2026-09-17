package com.example.paymentprocessor.dto;

import com.example.paymentprocessor.entity.TransactionStatus;

public class PaymentResponse {

    private Long transactionId;
    private TransactionStatus status;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(
            Long transactionId,
            TransactionStatus status,
            String message) {

        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}