package com.financial.intelligence.controller;

import com.financial.intelligence.model.PortfolioSummary;
import com.financial.intelligence.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Secures connection from our Vite React frontend
public class PortfolioController {
    
    private final PortfolioService portfolioService;

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummary> getPortfolioSummary(@RequestHeader("X-User-Id") UUID userId) {
        // High-security architecture: pulling the authenticated userId from our gateway headers
        PortfolioSummary summary = portfolioService.calculateUserPortfolio(userId);
        return ResponseEntity.ok(summary);
    }
}
