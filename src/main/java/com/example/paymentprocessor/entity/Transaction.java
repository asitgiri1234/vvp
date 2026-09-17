package com.example.paymentprocessor.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderId;

    private String receiverId;

    private BigDecimal amount;

    @Column(unique = true, nullable = false)
private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    public Transaction() {
    }

    public Transaction(
            String senderId,
            String receiverId,
            BigDecimal amount,
            String idempotencyKey,
            TransactionStatus status) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
    }

    public Long getId() {
        return id;
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

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}