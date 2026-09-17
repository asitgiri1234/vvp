package com.example.paymentprocessor.service;

import com.example.paymentprocessor.dto.PaymentRequest;
import com.example.paymentprocessor.dto.PaymentResponse;
import com.example.paymentprocessor.dto.TransactionResponse;
import com.example.paymentprocessor.entity.Transaction;
import com.example.paymentprocessor.entity.TransactionStatus;
import com.example.paymentprocessor.entity.Wallet;
import com.example.paymentprocessor.repository.TransactionRepository;
import com.example.paymentprocessor.repository.WalletRepository;
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

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processesSuccessfulPayment() {
        PaymentRequest request = request("payment-1", "100.00");
        Wallet sender = new Wallet("user-1", new BigDecimal("500.00"));
        Wallet receiver = new Wallet("user-2", new BigDecimal("100.00"));
        Transaction savedTransaction = new Transaction(
                "user-1", "user-2", new BigDecimal("100.00"),
                "payment-1", TransactionStatus.SUCCESS);

        when(transactionRepository.findByIdempotencyKey("payment-1"))
                .thenReturn(Optional.empty());
        when(walletRepository.findByOwnerId("user-1")).thenReturn(Optional.of(sender));
        when(walletRepository.findByOwnerId("user-2")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(response.getMessage()).isEqualTo("Payment successful");
        assertThat(sender.getBalance()).isEqualByComparingTo("400.00");
        assertThat(receiver.getBalance()).isEqualByComparingTo("200.00");
        verify(transactionRepository).save(any(Transaction.class));
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
        verify(walletRepository, never()).findByOwnerId(any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void rejectsPaymentWhenBalanceIsInsufficient() {
        PaymentRequest request = request("payment-1", "600.00");
        Wallet sender = new Wallet("user-1", new BigDecimal("500.00"));
        Wallet receiver = new Wallet("user-2", new BigDecimal("100.00"));

        when(transactionRepository.findByIdempotencyKey("payment-1"))
                .thenReturn(Optional.empty());
        when(walletRepository.findByOwnerId("user-1")).thenReturn(Optional.of(sender));
        when(walletRepository.findByOwnerId("user-2")).thenReturn(Optional.of(receiver));

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(response.getMessage()).isEqualTo("Insufficient balance");
        verify(transactionRepository, never()).save(any(Transaction.class));
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
        verify(walletRepository, never()).findByOwnerId(any());
    }
}
