package com.tracker.service;

import com.tracker.model.MarketPrice;
import com.tracker.model.Portfolio;
import com.tracker.model.AssetType;
import com.tracker.model.Transaction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background monitoring service that periodically checks live prices
 * against user-configured alert thresholds.
 * Uses Java's ScheduledExecutorService to run checks every 60 seconds.
 */
public class MonitoringService {

    private final PriceCache priceCache;
    private final Map<String, Double> alertThresholds;
    private final Portfolio portfolio;
    private ScheduledExecutorService scheduler;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * @param priceCache      the price cache for fetching live data
     * @param alertThresholds a map of symbol -> minimum acceptable price
     * @param portfolio       the portfolio to monitor
     */
    public MonitoringService(PriceCache priceCache, Map<String, Double> alertThresholds, Portfolio portfolio) {
        this.priceCache = priceCache;
        this.alertThresholds = alertThresholds;
        this.portfolio = portfolio;
    }

    /**
     * Starts the background monitoring loop.
     * Checks prices every 60 seconds and prints alerts for threshold breaches.
     */
    public void startMonitoring() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        System.out.println("\n[MONITOR] Background price monitoring started (checking every 60s)...");

        scheduler.scheduleAtFixedRate(this::checkAlerts, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * Stops the background monitoring loop.
     */
    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("[MONITOR] Background monitoring stopped.");
        }
    }

    /**
     * Runs a single check cycle: fetches prices for all alerted symbols
     * and prints warnings if any price falls below its threshold.
     */
    private void checkAlerts() {
        String timestamp = LocalDateTime.now().format(TIME_FMT);
        System.out.println("\n--- [MONITOR CHECK @ " + timestamp + "] ---");

        for (Map.Entry<String, Double> entry : alertThresholds.entrySet()) {
            String symbol = entry.getKey();
            double threshold = entry.getValue();

            try {
                // Determine asset type from portfolio transactions
                List<Transaction> txns = portfolio.getTransactionsBySymbol().get(symbol);
                if (txns == null || txns.isEmpty()) continue;

                AssetType type = txns.get(0).getAssetType();
                MarketPrice mp;
                if (type == AssetType.CRYPTO) {
                    mp = priceCache.getCryptoPrice(symbol);
                } else {
                    mp = priceCache.getStockPrice(symbol);
                }

                if (mp.getPrice() < threshold) {
                    printAlertBanner(symbol, mp.getPrice(), threshold);
                } else {
                    System.out.printf("  [OK] %s: $%.2f (threshold: $%.2f)%n",
                            symbol, mp.getPrice(), threshold);
                }
            } catch (IOException e) {
                System.err.println("  [MONITOR ERROR] Could not check " + symbol + ": " + e.getMessage());
            }
        }
    }

    /**
     * Prints a distinct warning banner when a price drops below threshold.
     */
    private void printAlertBanner(String symbol, double currentPrice, double threshold) {
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.printf("  ║  ⚠️  PRICE ALERT: %-30s  ║%n", symbol);
        System.out.printf("  ║  Current Price : $%-30.2f  ║%n", currentPrice);
        System.out.printf("  ║  Alert Threshold: $%-30.2f  ║%n", threshold);
        System.out.printf("  ║  Status        : %-30s  ║%n", "BELOW THRESHOLD!");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
    }
}
