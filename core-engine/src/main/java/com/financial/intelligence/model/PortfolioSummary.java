package com.financial.intelligence.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PortfolioSummary {
    private UUID userId;
    private BigDecimal totalValue;
    private BigDecimal totalCost;
    private BigDecimal absoluteProfitLoss;
    private double overallRoiPercentage;
}
