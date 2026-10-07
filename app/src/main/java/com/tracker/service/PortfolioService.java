package com.tracker.service;

import com.tracker.model.AssetType;
import com.tracker.model.MarketPrice;
import com.tracker.model.Portfolio;
import com.tracker.model.Transaction;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core business logic service for portfolio valuation and performance analysis.
 * Handles ROI calculations, performance sorting, and parallel price fetching.
 */
public class PortfolioService {

    private final PriceCache priceCache;

    public PortfolioService(PriceCache priceCache) {
        this.priceCache = priceCache;
    }

    /**
     * Calculates the current value of all holdings for a given symbol.
     * Current Value = Total Quantity * Live Market Price
     *
     * @param transactions all transactions for a specific symbol
     * @param livePrice    the current market price
     * @return the current total value
     */
    public double calculateCurrentValue(List<Transaction> transactions, double livePrice) {
        double totalQuantity = transactions.stream()
                .mapToDouble(Transaction::getQuantity)
                .sum();
        return totalQuantity * livePrice;
    }

    /**
     * Calculates the total cost basis for a set of transactions.
     * Total Cost Basis = Sum of (Quantity * Purchase Price) for each transaction.
     *
     * @param transactions the list of transactions
     * @return the total cost basis
     */
    public double calculateCostBasis(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(Transaction::getCostBasis)
                .sum();
    }

    /**
     * Calculates the Return on Investment (ROI) as a percentage.
     * ROI (%) = ((Current Value - Total Cost Basis) / Total Cost Basis) * 100
     *
     * @param currentValue  the current market value
     * @param costBasis     the original total investment
     * @return the ROI percentage
     */
    public double calculateROI(double currentValue, double costBasis) {
        if (costBasis == 0) return 0.0;
        return ((currentValue - costBasis) / costBasis) * 100.0;
    }

    /**
     * Generates a full portfolio valuation summary.
     * Fetches live prices for all unique symbols and calculates metrics.
     *
     * @param portfolio the portfolio to evaluate
     * @return a list of AssetSummary records sorted by ROI descending
     */
    public List<AssetSummary> evaluatePortfolio(Portfolio portfolio) {
        List<AssetSummary> summaries = new ArrayList<>();
        Map<String, List<Transaction>> grouped = portfolio.getTransactionsBySymbol();

        // Pre-fetch all prices in parallel
        Map<String, MarketPrice> livePrices = fetchAllPricesInParallel(portfolio);

        for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
            String symbol = entry.getKey();
            List<Transaction> txns = entry.getValue();
            AssetType type = txns.get(0).getAssetType();

            MarketPrice mp = livePrices.get(symbol);

            if (mp != null) {
                double currentValue = calculateCurrentValue(txns, mp.getPrice());
                double costBasis = calculateCostBasis(txns);
                double roi = calculateROI(currentValue, costBasis);
                double totalQty = txns.stream().mapToDouble(Transaction::getQuantity).sum();

                summaries.add(new AssetSummary(symbol, type, totalQty, costBasis, currentValue, roi, mp.getPrice()));
            } else {
                double costBasis = calculateCostBasis(txns);
                double totalQty = txns.stream().mapToDouble(Transaction::getQuantity).sum();
                summaries.add(new AssetSummary(symbol, type, totalQty, costBasis, 0, -100.0, 0));
            }
        }

        // Sort by ROI descending: Top Gainer first, Worst Performer last
        summaries.sort(Comparator.comparingDouble(AssetSummary::roi).reversed());
        return summaries;
    }

    /**
     * Fetches live prices for all unique symbols in parallel using CompletableFuture.
     * @param portfolio the portfolio containing the assets
     * @return a map of symbol to MarketPrice
     */
    public Map<String, MarketPrice> fetchAllPricesInParallel(Portfolio portfolio) {
        Map<String, MarketPrice> priceMap = new ConcurrentHashMap<>();
        Map<String, List<Transaction>> grouped = portfolio.getTransactionsBySymbol();

        List<CompletableFuture<Void>> futures = grouped.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    String symbol = entry.getKey();
                    AssetType type = entry.getValue().get(0).getAssetType();
                    try {
                        MarketPrice mp;
                        if (type == AssetType.CRYPTO) {
                            mp = priceCache.getCryptoPrice(symbol);
                        } else {
                            mp = priceCache.getStockPrice(symbol);
                        }
                        priceMap.put(symbol, mp);
                    } catch (IOException e) {
                        System.err.println("  [PARALLEL ERROR] " + symbol + ": " + e.getMessage());
                    }
                }))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return priceMap;
    }

    /**
     * Record to hold a computed summary for a single asset in the portfolio.
     */
    public record AssetSummary(
            String symbol,
            AssetType type,
            double totalQuantity,
            double costBasis,
            double currentValue,
            double roi,
            double livePrice
    ) {}
}
