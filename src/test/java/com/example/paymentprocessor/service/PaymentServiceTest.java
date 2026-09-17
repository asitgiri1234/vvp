package com.example.paymentprocessor.service;

import com.example.paymentprocessor.dto.PaymentRequest;
import com.example.paymentprocessor.dto.PaymentResponse;
import com.example.paymentprocessor.dto.TransactionResponse;
import com.example.paymentprocessor.entity.Transaction;
import com.example.paymentprocessor.entity.TransactionStatus;
import com.example.paymentprocessor.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.concurrent.atomic.AtomicBoolean;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentTransactionProcessor transactionProcessor;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processesSuccessfulPayment() {
        PaymentRequest request = request("payment-1", "100.00");
        when(transactionProcessor.process(request)).thenReturn(
                new PaymentResponse(1L, TransactionStatus.SUCCESS, "Payment successful"));

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(response.getMessage()).isEqualTo("Payment successful");
        verify(transactionProcessor).process(request);
    }

    @Test
    void returnsExistingPaymentForDuplicateIdempotencyKey() {
        PaymentRequest request = request("payment-1", "100.00");
        Transaction existingTransaction = new Transaction(
                "user-1", "user-2", new BigDecimal("100.00"),
                "payment-1", TransactionStatus.SUCCESS);

        when(transactionRepository.findByIdempotencyKey("payment-1"))
                .thenReturn(Optional.of(existingTransaction));

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(response.getMessage()).isEqualTo("Payment already processed");
        verify(transactionProcessor, never()).process(any());
    }

    @Test
    void rejectsPaymentWhenBalanceIsInsufficient() {
        PaymentRequest request = request("payment-1", "600.00");
        when(transactionProcessor.process(request)).thenReturn(
                new PaymentResponse(null, TransactionStatus.FAILED, "Insufficient balance"));

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(response.getMessage()).isEqualTo("Insufficient balance");
    }

    @Test
    void returnsCommittedTransactionWhenUniqueKeyRaceIsDetected() {
        PaymentRequest request = request("payment-1", "100.00");
        Transaction existingTransaction = new Transaction(
                "user-1", "user-2", new BigDecimal("100.00"),
                "payment-1", TransactionStatus.SUCCESS);
        when(transactionProcessor.process(request))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
        AtomicBoolean firstLookup = new AtomicBoolean(true);
        when(transactionRepository.findByIdempotencyKey("payment-1"))
                .thenAnswer(invocation -> firstLookup.getAndSet(false)
                        ? Optional.empty()
                        : Optional.of(existingTransaction));

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(response.getMessage()).isEqualTo("Payment already processed");
        verify(transactionRepository, org.mockito.Mockito.times(2))
                .findByIdempotencyKey("payment-1");
    }

    @Test
    void rejectsInvalidPaymentRequest() {
        PaymentResponse response = paymentService.processPayment(
                new PaymentRequest(" ", "user-2", BigDecimal.ZERO, " "));

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(response.getMessage()).isEqualTo("Amount must be greater than zero");
        verifyNoRepositoryInteractions();
    }

    @Test
    void retrievesTransactionWithoutExposingEntity() {
        Transaction transaction = new Transaction(
                "user-1", "user-2", new BigDecimal("100.00"),
                "payment-1", TransactionStatus.SUCCESS);
        when(transactionRepository.findById(42L)).thenReturn(Optional.of(transaction));

        Optional<TransactionResponse> response = paymentService.findTransaction(42L);

        assertThat(response).isPresent();
        assertThat(response.get().getSenderId()).isEqualTo("user-1");
        assertThat(response.get().getAmount()).isEqualByComparingTo("100.00");
        assertThat(response.get()).isNotInstanceOf(Transaction.class);
    }

    private PaymentRequest request(String idempotencyKey, String amount) {
        return new PaymentRequest(
                "user-1", "user-2", new BigDecimal(amount), idempotencyKey);
    }

    private void verifyNoRepositoryInteractions() {
        verify(transactionRepository, never()).findByIdempotencyKey(any());
        verify(transactionProcessor, never()).process(any());
    }
}
