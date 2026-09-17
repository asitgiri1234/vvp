package com.example.paymentprocessor.service;

import com.example.paymentprocessor.dto.PaymentRequest;
import com.example.paymentprocessor.dto.PaymentResponse;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionProcessorTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PaymentTransactionProcessor transactionProcessor;

    @Test
    void transfersFundsAndCreatesTransaction() {
        PaymentRequest request = new PaymentRequest(
                "user-1", "user-2", new BigDecimal("100.00"), "payment-1");
        Wallet sender = new Wallet("user-1", new BigDecimal("500.00"));
        Wallet receiver = new Wallet("user-2", new BigDecimal("100.00"));
        Transaction savedTransaction = new Transaction(
                "user-1", "user-2", new BigDecimal("100.00"),
                "payment-1", TransactionStatus.SUCCESS);

        when(walletRepository.findByOwnerId("user-1")).thenReturn(Optional.of(sender));
        when(walletRepository.findByOwnerId("user-2")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        PaymentResponse response = transactionProcessor.process(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(sender.getBalance()).isEqualByComparingTo("400.00");
        assertThat(receiver.getBalance()).isEqualByComparingTo("200.00");
    }
}
