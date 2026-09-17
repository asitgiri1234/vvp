package com.example.paymentprocessor.service;

import com.example.paymentprocessor.dto.PaymentRequest;
import com.example.paymentprocessor.dto.PaymentResponse;
import com.example.paymentprocessor.dto.TransactionResponse;
import com.example.paymentprocessor.entity.Transaction;
import com.example.paymentprocessor.entity.TransactionStatus;
import com.example.paymentprocessor.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentTransactionProcessor transactionProcessor;

    public PaymentServiceImpl(
            TransactionRepository transactionRepository,
            PaymentTransactionProcessor transactionProcessor) {
        this.transactionRepository = transactionRepository;
        this.transactionProcessor = transactionProcessor;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        PaymentResponse validationError = validate(request);
        if (validationError != null) {
            return validationError;
        }

        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingTransaction.isPresent()) {
            Transaction transaction = existingTransaction.get();
            return new PaymentResponse(
                    transaction.getId(),
                    transaction.getStatus(),
                    "Payment already processed");
        }

        try {
            return transactionProcessor.process(request);
        } catch (DataIntegrityViolationException exception) {
            return transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .map(transaction -> new PaymentResponse(
                            transaction.getId(),
                            transaction.getStatus(),
                            "Payment already processed"))
                    .orElseThrow(() -> exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionResponse> findTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getSenderId(),
                        transaction.getReceiverId(),
                        transaction.getAmount(),
                        transaction.getIdempotencyKey(),
                        transaction.getStatus()));
    }

    private PaymentResponse validate(PaymentRequest request) {
        if (request == null) {
            return failedResponse("Payment request is required");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            return failedResponse("Amount must be greater than zero");
        }
        if (isBlank(request.getSenderId())) {
            return failedResponse("Sender ID is required");
        }
        if (isBlank(request.getReceiverId())) {
            return failedResponse("Receiver ID is required");
        }
        if (isBlank(request.getIdempotencyKey())) {
            return failedResponse("Idempotency key is required");
        }
        if (request.getSenderId().equals(request.getReceiverId())) {
            return failedResponse("Sender and receiver must be different");
        }
        return null;
    }

    private PaymentResponse failedResponse(String message) {
        return new PaymentResponse(null, TransactionStatus.FAILED, message);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
