package com.financial.intelligence.service;

import com.financial.intelligence.model.PortfolioSummary;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PortfolioService {

    public PortfolioSummary calculateUserPortfolio(UUID userId) {
        // Architecture Wiring: In production, this aggregates DB transactions & hits Market Gateway
        return PortfolioSummary.builder()
                .userId(userId)
                .totalValue(new BigDecimal("15000.00"))
                .totalCost(new BigDecimal("12000.00"))
                .absoluteProfitLoss(new BigDecimal("3000.00"))
                .overallRoiPercentage(25.0)
                .build();
    }
}
