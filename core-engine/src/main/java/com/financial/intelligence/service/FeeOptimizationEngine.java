package com.financial.intelligence.service;

import com.financial.intelligence.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FeeOptimizationEngine {
    // Industry Benchmark: Over 0.75% expense ratio is mathematically considered an aggressive fee drag
    private static final BigDecimal HIGH_FEE_THRESHOLD = new BigDecimal("0.0075");

    /**
     * Scans transactions to identify high fee models and flags alternative routes to save user money.
     */
    public String evaluateFeeImpact(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "No assets tracked yet. Add transactions to analyze hidden costs.";
        }
        
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalFeesPaid = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            if (tx.getQuantity() != null && tx.getPurchasePrice() != null) {
                BigDecimal principal = tx.getQuantity().multiply(tx.getPurchasePrice());
                totalInvested = totalInvested.add(principal);
            }
            if (tx.getFeePaid() != null) {
                totalFeesPaid = totalFeesPaid.add(tx.getFeePaid());
            }
        }

        if (totalInvested.compareTo(BigDecimal.ZERO) == 0) return "Healthy Allocation";

        // Calculate aggregate fee ratio percentage
        BigDecimal overallFeeRatio = totalFeesPaid.divide(totalInvested, 4, RoundingMode.HALF_UP);
        
        if (overallFeeRatio.compareTo(HIGH_FEE_THRESHOLD) > 0) {
            BigDecimal percentageDisplay = overallFeeRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
            return String.format(
                "ALERT: Your portfolio fee ratio is %s%%. This is higher than the community median. " +
                "Action item: Consider moving high-commission manual assets into low-cost index ETFs.",
                percentageDisplay
            );
        }
        return "EXCELLENT: Your broker fee exposure is within safe, low-drag optimal parameters.";
    }
}
