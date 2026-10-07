package com.financial.intelligence.controller;

import com.financial.intelligence.service.FeeOptimizationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyticsController {
    
    private final FeeOptimizationEngine feeEngine;

    @GetMapping("/fee-audit")
    public ResponseEntity<String> runFeeAudit() {
        // Ideally we fetch the user's transactions from the DB here
        String report = feeEngine.evaluateFeeImpact(Collections.emptyList());
        return ResponseEntity.ok(report);
    }
}
