package com.tracker.client;

import com.tracker.model.MarketPrice;

import java.io.IOException;

/**
 * Interface for fetching live market price data from external APIs.
 * Implementations handle specific data sources (Alpha Vantage, CoinGecko, etc.).
 */
public interface MarketDataClient {

    /**
     * Fetches the current market price for the given asset symbol.
     * @param symbol the ticker symbol (e.g., "AAPL", "bitcoin")
     * @return a MarketPrice object with the current price
     * @throws IOException if the network request fails
     */
    MarketPrice fetchPrice(String symbol) throws IOException;
}
