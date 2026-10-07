package com.tracker.service;

import com.tracker.client.MarketDataClient;
import com.tracker.model.AssetType;
import com.tracker.model.MarketPrice;
import com.tracker.model.Portfolio;
import com.tracker.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the PortfolioService business logic.
 * Uses Mockito to mock external API clients.
 */
@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    private PortfolioService portfolioService;

    @Mock
    private MarketDataClient mockStockClient;

    @Mock
    private MarketDataClient mockCryptoClient;

    @BeforeEach
    void setUp() {
        PriceCache priceCache = new PriceCache(mockStockClient, mockCryptoClient);
        portfolioService = new PortfolioService(priceCache);
    }

    @Test
    void testCalculateCurrentValue() {
        List<Transaction> txns = List.of(
            new Transaction("AAPL", AssetType.STOCK, 10, 150.00, LocalDate.now()),
            new Transaction("AAPL", AssetType.STOCK, 5, 160.00, LocalDate.now())
        );

        // Total quantity = 15, live price = $200
        double currentValue = portfolioService.calculateCurrentValue(txns, 200.0);
        assertEquals(3000.0, currentValue, 0.01);
    }

    @Test
    void testCalculateCostBasis() {
        List<Transaction> txns = List.of(
            new Transaction("AAPL", AssetType.STOCK, 10, 150.00, LocalDate.now()),
            new Transaction("AAPL", AssetType.STOCK, 5, 160.00, LocalDate.now())
        );

        // Cost basis = (10 * 150) + (5 * 160) = 1500 + 800 = 2300
        double costBasis = portfolioService.calculateCostBasis(txns);
        assertEquals(2300.0, costBasis, 0.01);
    }

    @Test
    void testCalculateROI_Positive() {
        // Current value: $3000, Cost: $2300
        // ROI = ((3000 - 2300) / 2300) * 100 = 30.43%
        double roi = portfolioService.calculateROI(3000.0, 2300.0);
        assertEquals(30.43, roi, 0.01);
    }

    @Test
    void testCalculateROI_Negative() {
        // Current value: $1800, Cost: $2300
        // ROI = ((1800 - 2300) / 2300) * 100 = -21.74%
        double roi = portfolioService.calculateROI(1800.0, 2300.0);
        assertEquals(-21.74, roi, 0.01);
    }

    @Test
    void testCalculateROI_ZeroCostBasis() {
        double roi = portfolioService.calculateROI(1000.0, 0.0);
        assertEquals(0.0, roi, 0.01);
    }

    @Test
    void testEvaluatePortfolio() throws IOException {
        // Mock API responses
        when(mockStockClient.fetchPrice("IBM"))
                .thenReturn(new MarketPrice("IBM", 180.0, Instant.now()));

        Portfolio portfolio = new Portfolio("Test");
        portfolio.addTransaction(
                new Transaction("IBM", AssetType.STOCK, 10, 140.00, LocalDate.of(2024, 1, 15)));

        List<PortfolioService.AssetSummary> summaries = portfolioService.evaluatePortfolio(portfolio);

        assertEquals(1, summaries.size());
        PortfolioService.AssetSummary summary = summaries.get(0);
        assertEquals("IBM", summary.symbol());
        assertEquals(1400.0, summary.costBasis(), 0.01);
        assertEquals(1800.0, summary.currentValue(), 0.01);
        assertEquals(28.57, summary.roi(), 0.01); // ((1800-1400)/1400)*100
        assertEquals(400.0, summary.profitLoss(), 0.01);
        assertEquals(140.0, summary.avgBuyPrice(), 0.01);
        assertTrue(summary.fetchSuccess());
    }
}
