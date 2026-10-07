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
 * Client for fetching stock prices from the Alpha Vantage API.
 * Requires a free API key from https://www.alphavantage.co/
 *
 * Uses the GLOBAL_QUOTE endpoint to get the latest price for a stock symbol.
 */
public class AlphaVantageClient implements MarketDataClient {

    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AlphaVantageClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public MarketPrice fetchPrice(String symbol) throws IOException {
        String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                BASE_URL, symbol, apiKey);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Alpha Vantage API request failed: HTTP " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.has("Information") || root.has("Note")) {
                String msg = root.has("Information") ? root.get("Information").asText() : root.get("Note").asText();
                throw new IOException("Alpha Vantage API Rate Limit hit: " + msg);
            }

            JsonNode globalQuote = root.get("Global Quote");

            if (globalQuote == null || globalQuote.isEmpty()) {
                throw new IOException("No data returned for symbol: " + symbol
                        + ". Response: " + responseBody);
            }

            double price = Double.parseDouble(globalQuote.get("05. price").asText());
            return new MarketPrice(symbol.toUpperCase(), price, Instant.now());
        }
    }
}
