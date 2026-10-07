package com.financial.intelligence.controller;

import com.financial.intelligence.model.PortfolioSummary;
import com.financial.intelligence.model.Transaction;
import com.financial.intelligence.repository.TransactionRepository;
import com.financial.intelligence.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Secures connection from our Vite React frontend
public class PortfolioController {
    
    private final PortfolioService portfolioService;
    private final TransactionRepository transactionRepository;

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummary> getPortfolioSummary(@RequestHeader("X-User-Id") UUID userId) {
        PortfolioSummary summary = portfolioService.calculateUserPortfolio(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(transactionRepository.findByUserId(userId));
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> addTransaction(@RequestHeader("X-User-Id") UUID userId, @RequestBody Transaction tx) {
        tx.setUserId(userId);
        if (tx.getExecutedAt() == null) {
            tx.setExecutedAt(OffsetDateTime.now());
        }
        return ResponseEntity.ok(transactionRepository.save(tx));
    }
}
