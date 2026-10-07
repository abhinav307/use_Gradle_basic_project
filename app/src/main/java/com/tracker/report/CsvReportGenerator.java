package com.tracker.report;

import com.tracker.service.PortfolioService.AssetSummary;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates a CSV (Comma-Separated Values) report of the portfolio summary.
 * Output includes columns for symbol, type, quantity, cost basis,
 * current value, live price, and ROI percentage.
 */
public class CsvReportGenerator implements ReportGenerator {

    @Override
    public void generate(List<AssetSummary> summaries, String outputPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            // Header
            writer.println("Symbol,Type,Quantity,Cost Basis ($),Current Value ($),Live Price ($),ROI (%)");

            // Data rows
            double totalCost = 0;
            double totalValue = 0;

            for (AssetSummary s : summaries) {
                writer.printf("%s,%s,%.4f,%.2f,%.2f,%.2f,%.2f%n",
                        s.symbol(), s.type(), s.totalQuantity(),
                        s.costBasis(), s.currentValue(), s.livePrice(), s.roi());
                totalCost += s.costBasis();
                totalValue += s.currentValue();
            }

            // Totals row
            double totalROI = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;
            writer.printf("TOTAL,---,---,%.2f,%.2f,---,%.2f%n", totalCost, totalValue, totalROI);

            // Metadata
            writer.println();
            writer.println("Report Generated: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        System.out.println("[REPORT] CSV report saved to: " + outputPath);
    }
}
