package com.example.paymentprocessor.service;

import com.example.paymentprocessor.dto.PaymentRequest;
import com.example.paymentprocessor.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);
}