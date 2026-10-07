package com.financial.intelligence.controller;

import com.financial.intelligence.repository.TransactionRepository;
import com.financial.intelligence.service.FeeOptimizationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyticsController {
    
    private final FeeOptimizationEngine feeEngine;
    private final TransactionRepository transactionRepository;

    @GetMapping("/fee-audit")
    public ResponseEntity<String> runFeeAudit(@RequestHeader("X-User-Id") UUID userId) {
        String report = feeEngine.evaluateFeeImpact(transactionRepository.findByUserId(userId));
        return ResponseEntity.ok(report);
    }
}
