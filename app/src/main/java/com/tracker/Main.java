package com.tracker;

import com.tracker.client.AlphaVantageClient;
import com.tracker.client.CoinGeckoClient;
import com.tracker.model.AssetType;
import com.tracker.model.Portfolio;
import com.tracker.model.Transaction;
import com.tracker.report.CsvReportGenerator;
import com.tracker.report.PdfReportGenerator;
import com.tracker.repository.DatabaseManager;
import com.tracker.repository.TransactionRepository;
import com.tracker.service.MonitoringService;
import com.tracker.service.PortfolioService;
import com.tracker.service.PortfolioService.AssetSummary;
import com.tracker.service.PriceCache;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Main entry point for the Automated Financial Portfolio Tracker.
 *
 * This application demonstrates:
 * - Live market data integration (Alpha Vantage for stocks, CoinGecko for crypto)
 * - Financial calculations (ROI, cost basis, current valuation)
 * - Data persistence with embedded H2 database
 * - Background price monitoring with configurable alerts
 * - Report generation in CSV and PDF formats
 *
 * Usage:
 *   Set your Alpha Vantage API key as the ALPHA_VANTAGE_API_KEY environment variable.
 *   Or use the default "demo" key for testing with limited symbols.
 */
public class Main {

    private static final String REPORTS_DIR = "reports";

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   Automated Financial Portfolio Tracker");
        System.out.println("═══════════════════════════════════════════════════════");

        // --- Initialize Database ---
        DatabaseManager dbManager = new DatabaseManager();
        try {
            dbManager.initialize();
            System.out.println("[DB] Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Failed to initialize database: " + e.getMessage());
            return;
        }

        TransactionRepository txnRepo = new TransactionRepository(dbManager.getConnection());

        // --- Load or Seed Portfolio ---
        Portfolio portfolio = new Portfolio("My Portfolio");
        try {
            List<Transaction> existing = txnRepo.findAll();
            if (existing.isEmpty()) {
                System.out.println("\n[SETUP] No existing transactions found. Adding sample data...");
                seedSampleData(txnRepo, portfolio);
            } else {
                System.out.println("\n[DB] Loaded " + existing.size() + " transactions from database.");
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
            apiKey = "demo"; // Alpha Vantage demo key (limited to IBM symbol)
            System.out.println("\n[CONFIG] No ALPHA_VANTAGE_API_KEY env variable found.");
            System.out.println("         Using 'demo' key (only works with IBM symbol).");
            System.out.println("         Get a free key at: https://www.alphavantage.co/support/#api-key");
        }

        AlphaVantageClient stockClient = new AlphaVantageClient(apiKey);
        CoinGeckoClient cryptoClient = new CoinGeckoClient();
        PriceCache priceCache = new PriceCache(stockClient, cryptoClient);

        // --- Evaluate Portfolio ---
        PortfolioService portfolioService = new PortfolioService(priceCache);

        System.out.println("\n───────────────────────────────────────────────────────");
        System.out.println("   Fetching Live Market Data & Computing Portfolio...");
        System.out.println("───────────────────────────────────────────────────────");

        List<AssetSummary> summaries = portfolioService.evaluatePortfolio(portfolio);
        printPortfolioTable(summaries);

        // --- Generate Reports ---
        System.out.println("\n───────────────────────────────────────────────────────");
        System.out.println("   Generating Reports...");
        System.out.println("───────────────────────────────────────────────────────");

        java.io.File reportsDir = new java.io.File(REPORTS_DIR);
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        try {
            new CsvReportGenerator().generate(summaries, REPORTS_DIR + "/portfolio_report.csv");
            new PdfReportGenerator().generate(summaries, REPORTS_DIR + "/portfolio_report.pdf");
        } catch (IOException e) {
            System.err.println("[REPORT ERROR] " + e.getMessage());
        }

        // --- Interactive Menu ---
        runInteractiveMenu(txnRepo, portfolio, portfolioService, priceCache, summaries);

        // --- Cleanup ---
        dbManager.shutdown();
        System.out.println("\n[APP] Portfolio Tracker shut down. Goodbye!");
    }

    /**
     * Seeds the database with sample transaction data for demonstration.
     */
    private static void seedSampleData(TransactionRepository repo, Portfolio portfolio) throws SQLException {
        Transaction[] samples = {
            new Transaction("IBM", AssetType.STOCK, 10, 140.00, LocalDate.of(2024, 1, 15)),
            new Transaction("bitcoin", AssetType.CRYPTO, 0.5, 42000.00, LocalDate.of(2024, 2, 1)),
            new Transaction("ethereum", AssetType.CRYPTO, 5.0, 2200.00, LocalDate.of(2024, 3, 10)),
        };

        for (Transaction t : samples) {
            repo.save(t);
            portfolio.addTransaction(t);
            System.out.println("  + Added: " + t);
        }
    }

    /**
     * Prints a formatted table of the portfolio evaluation results.
     */
    private static void printPortfolioTable(List<AssetSummary> summaries) {
        System.out.println();
        System.out.printf("%-12s %-8s %10s %14s %14s %12s %10s%n",
                "Symbol", "Type", "Quantity", "Cost Basis", "Curr Value", "Live Price", "ROI (%)");
        System.out.println("─".repeat(82));

        double totalCost = 0, totalValue = 0;
        String topGainer = "", worstPerformer = "";
        double bestROI = Double.NEGATIVE_INFINITY, worstROI = Double.POSITIVE_INFINITY;

        for (AssetSummary s : summaries) {
            String roiStr = String.format("%+.2f%%", s.roi());
            System.out.printf("%-12s %-8s %10.4f %14s %14s %12s %10s%n",
                    s.symbol(), s.type(),
                    s.totalQuantity(),
                    String.format("$%,.2f", s.costBasis()),
                    String.format("$%,.2f", s.currentValue()),
                    String.format("$%,.2f", s.livePrice()),
                    roiStr);
            totalCost += s.costBasis();
            totalValue += s.currentValue();

            if (s.roi() > bestROI) { bestROI = s.roi(); topGainer = s.symbol(); }
            if (s.roi() < worstROI) { worstROI = s.roi(); worstPerformer = s.symbol(); }
        }

        System.out.println("─".repeat(82));
        double totalROI = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;
        System.out.printf("%-12s %-8s %10s %14s %14s %12s %+10.2f%%%n",
                "TOTAL", "", "",
                String.format("$%,.2f", totalCost),
                String.format("$%,.2f", totalValue),
                "", totalROI);

        System.out.println();
        System.out.println("  🏆 Top Gainer     : " + topGainer + " (" + String.format("%+.2f%%", bestROI) + ")");
        System.out.println("  📉 Worst Performer: " + worstPerformer + " (" + String.format("%+.2f%%", worstROI) + ")");
    }

    /**
     * Runs an interactive console menu for the user.
     */
    private static void runInteractiveMenu(TransactionRepository txnRepo, Portfolio portfolio,
                                           PortfolioService svc, PriceCache cache,
                                           List<AssetSummary> summaries) {
        Scanner scanner = new Scanner(System.in);
        MonitoringService monitor = null;

        while (true) {
            System.out.println("\n═══════════════════════════════════════════════════════");
            System.out.println("   MENU");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("  1. Add a new transaction (Note: Crypto needs full IDs, e.g. 'bitcoin', 'ethereum')");
            System.out.println("  2. View portfolio summary");
            System.out.println("  3. Refresh prices (re-evaluate)");
            System.out.println("  4. Generate reports (CSV + PDF)");
            System.out.println("  5. Start background price monitoring");
            System.out.println("  6. Stop background monitoring");
            System.out.println("  0. Exit");
            System.out.print("\n  Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addTransaction(scanner, txnRepo, portfolio);
                case "2" -> printPortfolioTable(summaries);
                case "3" -> {
                    cache.clearCache();
                    summaries = svc.evaluatePortfolio(portfolio);
                    printPortfolioTable(summaries);
                }
                case "4" -> {
                    try {
                        new CsvReportGenerator().generate(summaries, REPORTS_DIR + "/portfolio_report.csv");
                        new PdfReportGenerator().generate(summaries, REPORTS_DIR + "/portfolio_report.pdf");
                    } catch (IOException e) {
                        System.err.println("[ERROR] " + e.getMessage());
                    }
                }
                case "5" -> {
                    if (monitor != null) {
                        System.out.println("[MONITOR] Already running. Stop it first (option 6).");
                    } else {
                        Map<String, Double> thresholds = new HashMap<>();
                        System.out.print("  Enter symbol to monitor (e.g., IBM): ");
                        String sym = scanner.nextLine().trim();
                        System.out.print("  Enter alert threshold price: $");
                        double thresh = Double.parseDouble(scanner.nextLine().trim());
                        thresholds.put(sym, thresh);
                        monitor = new MonitoringService(cache, thresholds, portfolio);
                        monitor.startMonitoring();
                    }
                }
                case "6" -> {
                    if (monitor != null) {
                        monitor.stopMonitoring();
                        monitor = null;
                    } else {
                        System.out.println("[MONITOR] No active monitor to stop.");
                    }
                }
                case "0" -> {
                    if (monitor != null) monitor.stopMonitoring();
                    scanner.close();
                    return;
                }
                default -> System.out.println("  Invalid choice. Try again.");
            }
        }
    }

    /**
     * Prompts the user to add a new transaction interactively.
     */
    private static void addTransaction(Scanner scanner, TransactionRepository repo, Portfolio portfolio) {
        try {
            System.out.print("  Symbol (e.g., AAPL, bitcoin): ");
            String symbol = scanner.nextLine().trim();

            System.out.print("  Type (STOCK / CRYPTO): ");
            AssetType type = AssetType.valueOf(scanner.nextLine().trim().toUpperCase());

            System.out.print("  Quantity: ");
            double qty = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("  Purchase Price ($): ");
            double price = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("  Purchase Date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(scanner.nextLine().trim());

            Transaction t = new Transaction(symbol, type, qty, price, date);
            repo.save(t);
            portfolio.addTransaction(t);
            System.out.println("  ✅ Transaction added: " + t);
        } catch (Exception e) {
            System.err.println("  [ERROR] Invalid input: " + e.getMessage());
        }
    }
}
