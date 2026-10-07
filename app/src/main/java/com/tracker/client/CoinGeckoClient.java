package com.tracker.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.model.MarketPrice;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Instant;

/**
 * Client for fetching cryptocurrency prices from the CoinGecko API.
 * Uses the free /simple/price endpoint (no API key required for basic use).
 *
 * Symbols should be CoinGecko IDs (e.g., "bitcoin", "ethereum", "solana").
 */
public class CoinGeckoClient implements MarketDataClient {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3/simple/price";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CoinGeckoClient() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public MarketPrice fetchPrice(String symbol) throws IOException {
        String coinId = symbol.toLowerCase();
        String url = String.format("%s?ids=%s&vs_currencies=usd", BASE_URL, coinId);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("CoinGecko API request failed: HTTP " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode coinNode = root.get(coinId);

            if (coinNode == null || !coinNode.has("usd")) {
                throw new IOException("No price data returned for crypto: " + symbol
                        + ". Ensure you are using a valid CoinGecko coin ID.");
            }

            double price = coinNode.get("usd").asDouble();
            return new MarketPrice(symbol.toUpperCase(), price, Instant.now());
        }
    }
}
