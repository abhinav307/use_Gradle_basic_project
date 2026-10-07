package com.tracker.report;

import com.tracker.service.PortfolioService.AssetSummary;

import java.io.IOException;
import java.util.List;

/**
 * Interface for generating portfolio reports in different formats.
 * Implementations handle specific output formats (CSV, PDF, etc.).
 */
public interface ReportGenerator {

    /**
     * Generates a report from the given portfolio summary data.
     *
     * @param summaries  the list of computed asset summaries
     * @param outputPath the file path to write the report to
     * @throws IOException if writing the report fails
     */
    void generate(List<AssetSummary> summaries, String outputPath) throws IOException;
}
