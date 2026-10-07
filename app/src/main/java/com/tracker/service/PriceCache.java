package com.tracker.service;

import com.tracker.client.MarketDataClient;
import com.tracker.model.MarketPrice;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A local cache layer for market prices to avoid hitting API rate limits.
 * Caches prices for 5 minutes before re-fetching from the network.
 * Thread-safe via ConcurrentHashMap for concurrent access.
 */
public class PriceCache {

    private final Map<String, MarketPrice> cache = new ConcurrentHashMap<>();
    private final MarketDataClient stockClient;
    private final MarketDataClient cryptoClient;

    public PriceCache(MarketDataClient stockClient, MarketDataClient cryptoClient) {
        this.stockClient = stockClient;
        this.cryptoClient = cryptoClient;
    }

    /**
     * Gets the price for a stock symbol, using the cache if available and fresh.
     * @param symbol the stock ticker (e.g., "AAPL")
     * @return the market price
     * @throws IOException if the API call fails
     */
    public MarketPrice getStockPrice(String symbol) throws IOException {
        return getCachedOrFetch(symbol.toUpperCase(), stockClient);
    }

    /**
     * Gets the price for a crypto symbol, using the cache if available and fresh.
     * @param coinId the CoinGecko coin ID (e.g., "bitcoin")
     * @return the market price
     * @throws IOException if the API call fails
     */
    public MarketPrice getCryptoPrice(String coinId) throws IOException {
        return getCachedOrFetch(coinId.toLowerCase(), cryptoClient);
    }

    /**
     * Checks cache first. If cached value is fresh (< 5 min old), returns it.
     * Otherwise fetches a new price from the client and updates the cache.
     */
    private MarketPrice getCachedOrFetch(String key, MarketDataClient client) throws IOException {
        MarketPrice cached = cache.get(key);
        if (cached != null && !cached.isStale()) {
            System.out.println("  [CACHE HIT] Using cached price for " + key);
            return cached;
        }

        System.out.println("  [CACHE MISS] Fetching live price for " + key + "...");
        MarketPrice fresh = client.fetchPrice(key);
        cache.put(key, fresh);
        return fresh;
    }

    /**
     * Clears all cached prices.
     */
    public void clearCache() {
        cache.clear();
    }
}
