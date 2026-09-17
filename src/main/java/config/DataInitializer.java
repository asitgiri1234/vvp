package com.example.paymentprocessor.config;

import com.example.paymentprocessor.entity.Wallet;
import com.example.paymentprocessor.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeWallets(WalletRepository walletRepository) {
        return args -> {
            walletRepository.save(
                    new Wallet("user-1", new BigDecimal("500.00"))
            );

            walletRepository.save(
                    new Wallet("user-2", new BigDecimal("100.00"))
            );
        };
    }
}