package com.financial.intelligence.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class CoinGeckoClient {
    private final WebClient webClient;

    public CoinGeckoClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.coingecko.com/api/v3").build();
    }

    @CircuitBreaker(name = "coinGecko", fallbackMethod = "fallbackPrice")
    @Retry(name = "coinGecko")
    public BigDecimal getCryptoPrice(String coinId) {
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/simple/price")
                        .queryParam("ids", coinId)
                        .queryParam("vs_currencies", "usd")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response != null && response.has(coinId)) {
            return new BigDecimal(response.get(coinId).get("usd").asText());
        }
        
        throw new RuntimeException("CoinGecko data not found for: " + coinId);
    }

    public BigDecimal fallbackPrice(String coinId, Throwable t) {
        System.err.println("[FALLBACK] CoinGecko Circuit Breaker tripped for " + coinId + " - " + t.getMessage());
        return BigDecimal.ZERO; 
    }
}
