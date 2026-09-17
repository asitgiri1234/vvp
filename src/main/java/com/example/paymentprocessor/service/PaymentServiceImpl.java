package com.example.paymentprocessor.service;

import org.springframework.transaction.annotation.Transactional;
import com.example.paymentprocessor.dto.PaymentRequest;
import com.example.paymentprocessor.dto.PaymentResponse;
import com.example.paymentprocessor.entity.Transaction;
import com.example.paymentprocessor.entity.TransactionStatus;
import com.example.paymentprocessor.entity.Wallet;
import com.example.paymentprocessor.repository.TransactionRepository;
import com.example.paymentprocessor.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public PaymentServiceImpl(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {

        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(
                        request.getIdempotencyKey()
                );

        if (existingTransaction.isPresent()) {

            Transaction transaction = existingTransaction.get();

            return new PaymentResponse(
                    transaction.getId(),
                    transaction.getStatus(),
                    "Payment already processed"
            );
        }

        Optional<Wallet> senderOptional =
                walletRepository.findByOwnerId(request.getSenderId());

        Optional<Wallet> receiverOptional =
                walletRepository.findByOwnerId(request.getReceiverId());

        if (senderOptional.isEmpty() || receiverOptional.isEmpty()) {
            return new PaymentResponse(
                    null,
                    TransactionStatus.FAILED,
                    "Sender or receiver wallet not found"
            );
        }

        Wallet sender = senderOptional.get();
        Wallet receiver = receiverOptional.get();

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            return new PaymentResponse(
                    null,
                    TransactionStatus.FAILED,
                    "Insufficient balance"
            );
        }

        sender.setBalance(
                sender.getBalance().subtract(request.getAmount())
        );

        receiver.setBalance(
                receiver.getBalance().add(request.getAmount())
        );

        walletRepository.save(sender);
        walletRepository.save(receiver);

        Transaction transaction = new Transaction(
                request.getSenderId(),
                request.getReceiverId(),
                request.getAmount(),
                request.getIdempotencyKey(),
                TransactionStatus.SUCCESS
        );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return new PaymentResponse(
                savedTransaction.getId(),
                TransactionStatus.SUCCESS,
                "Payment successful"
        );
    }
}