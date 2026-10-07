package com.tracker.report;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.tracker.service.PortfolioService.AssetSummary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates a professional PDF report of the portfolio summary using iText 7.
 * Includes a title, summary table with color-coded ROI values,
 * and totals row.
 */
public class PdfReportGenerator implements ReportGenerator {

    @Override
    public void generate(List<AssetSummary> summaries, String outputPath) throws IOException {
        try (PdfWriter writer = new PdfWriter(outputPath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Title
            document.add(new Paragraph("Portfolio Tracker Report")
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Table with 7 columns
            Table table = new Table(UnitValue.createPercentArray(
                    new float[]{15, 10, 12, 15, 15, 15, 12}))
                    .useAllAvailableWidth();

            // Header row
            String[] headers = {"Symbol", "Type", "Quantity", "Cost Basis", "Current Value", "Live Price", "ROI (%)"};
            for (String header : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // Data rows
            double totalCost = 0;
            double totalValue = 0;

            for (AssetSummary s : summaries) {
                table.addCell(createCell(s.symbol()));
                table.addCell(createCell(s.type().name()));
                table.addCell(createCell(String.format("%.4f", s.totalQuantity())));
                table.addCell(createCell(String.format("$%.2f", s.costBasis())));
                table.addCell(createCell(String.format("$%.2f", s.currentValue())));
                table.addCell(createCell(String.format("$%.2f", s.livePrice())));

                // Color-code ROI: green for positive, red for negative
                Cell roiCell = new Cell()
                        .add(new Paragraph(String.format("%.2f%%", s.roi())))
                        .setTextAlignment(TextAlignment.CENTER);
                if (s.roi() >= 0) {
                    roiCell.setFontColor(ColorConstants.GREEN);
                } else {
                    roiCell.setFontColor(ColorConstants.RED);
                }
                table.addCell(roiCell);

                totalCost += s.costBasis();
                totalValue += s.currentValue();
            }

            // Totals row
            double totalROI = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;
            table.addCell(new Cell().add(new Paragraph("TOTAL").setBold()));
            table.addCell(createCell("---"));
            table.addCell(createCell("---"));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", totalCost)).setBold()));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", totalValue)).setBold()));
            table.addCell(createCell("---"));
            Cell totalRoiCell = new Cell()
                    .add(new Paragraph(String.format("%.2f%%", totalROI)).setBold())
                    .setTextAlignment(TextAlignment.CENTER);
            totalRoiCell.setFontColor(totalROI >= 0 ? ColorConstants.GREEN : ColorConstants.RED);
            table.addCell(totalRoiCell);

            document.add(table);

            // Footer with asset count
            document.add(new Paragraph("\nTotal Assets: " + summaries.size())
                    .setFontSize(10));
        }

        System.out.println("[REPORT] PDF report saved to: " + outputPath);
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER);
    }
}
