package com.tracker.model;

import java.time.Instant;

/**
 * Represents the current market price of an asset fetched from an external API.
 * Includes a timestamp for cache invalidation purposes.
 */
public class MarketPrice {
    private String symbol;
    private double price;
    private Instant fetchedAt;

    public MarketPrice() {}

    public MarketPrice(String symbol, double price, Instant fetchedAt) {
        this.symbol = symbol;
        this.price = price;
        this.fetchedAt = fetchedAt;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }

    /**
     * Checks if this cached price is still valid (less than 5 minutes old).
     * @return true if the price was fetched within the last 5 minutes
     */
    public boolean isStale() {
        return Instant.now().minusSeconds(300).isAfter(fetchedAt);
    }

    @Override
    public String toString() {
        return String.format("MarketPrice{symbol='%s', price=%.2f, fetchedAt=%s}", symbol, price, fetchedAt);
    }
}
