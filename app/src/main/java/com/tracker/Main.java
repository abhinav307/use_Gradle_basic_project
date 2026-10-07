package com.tracker;

import com.tracker.client.AlphaVantageClient;
import com.tracker.client.CoinGeckoClient;
import com.tracker.model.AssetType;
import com.tracker.model.Portfolio;
import com.tracker.model.Transaction;
import com.tracker.repository.DatabaseManager;
import com.tracker.repository.TransactionRepository;
import com.tracker.service.PortfolioService;
import com.tracker.service.PriceCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.http.staticfiles.Location;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web-based Main entry point for the Automated Financial Portfolio Tracker.
 * Uses Javalin to serve REST APIs and a static HTML/JS frontend.
 */
public class Main {

    public static void main(String[] args) {
        // --- Initialize Database ---
        DatabaseManager dbManager = new DatabaseManager();
        try {
            dbManager.initialize();
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Failed to initialize database: " + e.getMessage());
            return;
        }

        TransactionRepository txnRepo = new TransactionRepository(dbManager.getConnection());
        Portfolio portfolio = new Portfolio("My Portfolio");

        try {
            List<Transaction> existing = txnRepo.findAll();
            if (existing.isEmpty()) {
                seedSampleData(txnRepo, portfolio);
            } else {
                existing.forEach(portfolio::addTransaction);
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] " + e.getMessage());
            dbManager.shutdown();
            return;
        }

        // --- Setup API Clients & Cache ---
        String apiKey = System.getenv("ALPHA_VANTAGE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = "demo";
        }
        AlphaVantageClient stockClient = new AlphaVantageClient(apiKey);
        CoinGeckoClient cryptoClient = new CoinGeckoClient();
        PriceCache priceCache = new PriceCache(stockClient, cryptoClient);
        PortfolioService portfolioService = new PortfolioService(priceCache);

        // --- Setup Web Server (Javalin) ---
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Javalin app = Javalin.create(config -> {
            // Configure Jackson to handle Java 8+ Dates
            config.jsonMapper(new JavalinJackson(mapper, false));
            
            // Serve static files (HTML, CSS, JS) from src/main/resources/public
            config.staticFiles.add("/public", Location.CLASSPATH);
            
            // Allow CORS for local development
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        }).start(7070);

        System.out.println("\n[WEB SERVER] Started successfully!");
        System.out.println("[WEB SERVER] Open your browser and navigate to: http://localhost:7070");

        // --- REST API Endpoints ---

        // 1. Get Portfolio Summary
        app.get("/api/portfolio", ctx -> {
            List<PortfolioService.AssetSummary> summaries = portfolioService.evaluatePortfolio(portfolio);
            
            double totalCost = summaries.stream().mapToDouble(PortfolioService.AssetSummary::costBasis).sum();
            double totalValue = summaries.stream().mapToDouble(PortfolioService.AssetSummary::currentValue).sum();
            double totalRoi = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;

            Map<String, Object> response = new HashMap<>();
            response.put("assets", summaries);
            response.put("totalCost", totalCost);
            response.put("totalValue", totalValue);
            response.put("totalRoi", totalRoi);

            ctx.json(response);
        });

        // 2. Add Transaction
        app.post("/api/transactions", ctx -> {
            TransactionRequest req = ctx.bodyAsClass(TransactionRequest.class);
            try {
                Transaction t = new Transaction(
                        req.symbol(),
                        AssetType.valueOf(req.type().toUpperCase()),
                        req.quantity(),
                        req.price(),
                        LocalDate.parse(req.date())
                );
                txnRepo.save(t);
                portfolio.addTransaction(t);
                ctx.status(201).json(Map.of("message", "Transaction added successfully", "transaction", t));
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        // 3. Refresh Cache
        app.post("/api/refresh", ctx -> {
            priceCache.clearCache();
            ctx.json(Map.of("message", "Cache cleared. Next fetch will pull live data."));
        });
        
        // Ensure database shutdown on exit
        Runtime.getRuntime().addShutdownHook(new Thread(dbManager::shutdown));
    }

    private static void seedSampleData(TransactionRepository repo, Portfolio portfolio) throws SQLException {
        Transaction[] samples = {
            new Transaction("IBM", AssetType.STOCK, 10, 140.00, LocalDate.of(2024, 1, 15)),
            new Transaction("bitcoin", AssetType.CRYPTO, 0.5, 42000.00, LocalDate.of(2024, 2, 1)),
            new Transaction("ethereum", AssetType.CRYPTO, 5.0, 2200.00, LocalDate.of(2024, 3, 10)),
        };
        for (Transaction t : samples) {
            repo.save(t);
            portfolio.addTransaction(t);
        }
    }

    // DTO for JSON request mapping
    public record TransactionRequest(String symbol, String type, double quantity, double price, String date) {}
}
