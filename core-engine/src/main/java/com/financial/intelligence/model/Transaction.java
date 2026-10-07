package com.financial.intelligence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId; // Ties the asset securely to an individual user account

    @Column(nullable = false)
    private String assetSymbol; // e.g., "AAPL", "BTC", "VOO"

    private String assetName; // e.g., "Apple Inc."

    @Column(nullable = false)
    private String assetCategory; // e.g., STOCK, CRYPTO, ETF

    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal quantity; // Use BigDecimal to prevent double rounding errors

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal purchasePrice; // Execution price per unit

    @Column(precision = 19, scale = 4)
    private BigDecimal feePaid; // Tracks broker commissions/network gas fees

    private String currency; // Base currency e.g., "USD", "INR"

    @Column(nullable = false)
    private OffsetDateTime executedAt; // Precise timezone-aware execution timestamp
}
