package com.example.paymentprocessor.service;

import com.example.paymentprocessor.dto.PaymentRequest;
import com.example.paymentprocessor.dto.PaymentResponse;
import com.example.paymentprocessor.entity.Transaction;
import com.example.paymentprocessor.entity.TransactionStatus;
import com.example.paymentprocessor.entity.Wallet;
import com.example.paymentprocessor.repository.TransactionRepository;
import com.example.paymentprocessor.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
class PaymentTransactionProcessor {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    PaymentTransactionProcessor(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    PaymentResponse process(PaymentRequest request) {
        Optional<Wallet> senderOptional =
                walletRepository.findByOwnerId(request.getSenderId());
        Optional<Wallet> receiverOptional =
                walletRepository.findByOwnerId(request.getReceiverId());
        if (senderOptional.isEmpty() || receiverOptional.isEmpty()) {
            return failedResponse("Sender or receiver wallet not found");
        }

        Wallet sender = senderOptional.get();
        Wallet receiver = receiverOptional.get();
        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            return failedResponse("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));
        walletRepository.save(sender);
        walletRepository.save(receiver);

        Transaction transaction = new Transaction(
                request.getSenderId(),
                request.getReceiverId(),
                request.getAmount(),
                request.getIdempotencyKey(),
                TransactionStatus.SUCCESS);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new PaymentResponse(
                savedTransaction.getId(),
                TransactionStatus.SUCCESS,
                "Payment successful");
    }

    private PaymentResponse failedResponse(String message) {
        return new PaymentResponse(null, TransactionStatus.FAILED, message);
    }
}
