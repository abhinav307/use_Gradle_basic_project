package com.financial.intelligence.service;

import com.financial.intelligence.client.AlphaVantageClient;
import com.financial.intelligence.client.CoinGeckoClient;
import com.financial.intelligence.model.PortfolioSummary;
import com.financial.intelligence.model.Transaction;
import com.financial.intelligence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final TransactionRepository transactionRepository;
    private final AlphaVantageClient alphaVantageClient;
    private final CoinGeckoClient coinGeckoClient;

    public PortfolioSummary calculateUserPortfolio(UUID userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            BigDecimal cost = tx.getQuantity().multiply(tx.getPurchasePrice());
            totalCost = totalCost.add(cost);

            BigDecimal currentPrice = BigDecimal.ZERO;
            try {
                if ("CRYPTO".equalsIgnoreCase(tx.getAssetCategory())) {
                    currentPrice = coinGeckoClient.getCryptoPrice(tx.getAssetSymbol().toLowerCase());
                } else {
                    currentPrice = alphaVantageClient.getStockPrice(tx.getAssetSymbol().toUpperCase());
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch price for " + tx.getAssetSymbol());
            }
            
            BigDecimal value = tx.getQuantity().multiply(currentPrice);
            totalValue = totalValue.add(value);
        }

        BigDecimal profitLoss = totalValue.subtract(totalCost);
        double roi = 0.0;
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            roi = profitLoss.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
        }

        return PortfolioSummary.builder()
                .userId(userId)
                .totalValue(totalValue)
                .totalCost(totalCost)
                .absoluteProfitLoss(profitLoss)
                .overallRoiPercentage(roi)
                .build();
    }
}
