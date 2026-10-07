package com.financial.intelligence.config;

import com.financial.intelligence.model.Transaction;
import com.financial.intelligence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final TransactionRepository transactionRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        UUID defaultUser = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        if (transactionRepository.count() == 0) {
            transactionRepository.save(Transaction.builder()
                    .userId(defaultUser)
                    .assetSymbol("AAPL")
                    .assetName("Apple Inc.")
                    .assetCategory("STOCK")
                    .quantity(new BigDecimal("50"))
                    .purchasePrice(new BigDecimal("150.00"))
                    .feePaid(new BigDecimal("2.50"))
                    .currency("USD")
                    .executedAt(OffsetDateTime.now().minusDays(10))
                    .build());

            transactionRepository.save(Transaction.builder()
                    .userId(defaultUser)
                    .assetSymbol("bitcoin")
                    .assetName("Bitcoin")
                    .assetCategory("CRYPTO")
                    .quantity(new BigDecimal("0.15"))
                    .purchasePrice(new BigDecimal("45000.00"))
                    .feePaid(new BigDecimal("15.00"))
                    .currency("USD")
                    .executedAt(OffsetDateTime.now().minusDays(5))
                    .build());

            transactionRepository.save(Transaction.builder()
                    .userId(defaultUser)
                    .assetSymbol("VOO")
                    .assetName("Vanguard S&P 500 ETF")
                    .assetCategory("ETF")
                    .quantity(new BigDecimal("12"))
                    .purchasePrice(new BigDecimal("410.00"))
                    .feePaid(new BigDecimal("0.00"))
                    .currency("USD")
                    .executedAt(OffsetDateTime.now().minusDays(100))
                    .build());
                    
            System.out.println("Seeded database with initial transactions for " + defaultUser);
        }
    }
}
