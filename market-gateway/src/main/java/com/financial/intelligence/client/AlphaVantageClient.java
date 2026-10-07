package com.financial.intelligence.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class AlphaVantageClient {
    private final WebClient webClient;
    private final String apiKey;

    public AlphaVantageClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
        this.apiKey = System.getenv().getOrDefault("ALPHA_VANTAGE_API_KEY", "demo");
    }

    @CircuitBreaker(name = "alphaVantage", fallbackMethod = "fallbackPrice")
    @Retry(name = "alphaVantage")
    public BigDecimal getStockPrice(String symbol) {
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("function", "GLOBAL_QUOTE")
                        .queryParam("symbol", symbol)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response != null && response.has("Global Quote")) {
            JsonNode quote = response.get("Global Quote");
            if (quote.has("05. price")) {
                return new BigDecimal(quote.get("05. price").asText());
            }
        }
        
        throw new RuntimeException("AlphaVantage API Rate Limit or Invalid Symbol: " + symbol);
    }

    public BigDecimal fallbackPrice(String symbol, Throwable t) {
        System.err.println("[FALLBACK] AlphaVantage Circuit Breaker tripped for " + symbol + " - " + t.getMessage());
        return BigDecimal.ZERO; 
    }
}
