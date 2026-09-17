package com.example.paymentprocessor.service;

import com.example.paymentprocessor.dto.PaymentRequest;
import com.example.paymentprocessor.dto.PaymentResponse;
import com.example.paymentprocessor.dto.TransactionResponse;

import java.util.Optional;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);

    Optional<TransactionResponse> findTransaction(Long transactionId);
}