package com.financetracker.export;

import com.financetracker.model.Transaction;
import com.financetracker.service.CategoryAggregator;
import com.financetracker.service.TransactionService;
import com.financetracker.util.DateUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * CSV Export Engine
 * -----------------
 * Generates clean, Excel-friendly monthly financial summary reports.
 */
public class CsvExportEngine {

    private final TransactionService transactionService;
    private final CategoryAggregator categoryAggregator;
    private final Path exportFolder;

    public CsvExportEngine(TransactionService transactionService,
                           CategoryAggregator categoryAggregator,
                           String exportFolderPath) {
        this.transactionService = transactionService;
        this.categoryAggregator = categoryAggregator;
        this.exportFolder = Paths.get(exportFolderPath);
    }

    /**
     * Exports a full monthly report:
     * 1. Summary sheet (income, expense, net)
     * 2. Category breakdown
     * 3. Detailed transaction list
     */
    public Path exportMonthlySummary(YearMonth yearMonth) throws IOException {
        Files.createDirectories(exportFolder);

        String fileName = String.format("Financial_Summary_%s.csv", yearMonth);
        Path filePath = exportFolder.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {

            // ---- Header ----
            writer.write("PERSONAL FINANCE TRACKER - MONTHLY SUMMARY");
            writer.newLine();
            writer.write("Generated for: " + yearMonth);
            writer.newLine();
            writer.write("Generated on: " + DateUtils.format(java.time.LocalDate.now()));
            writer.newLine();
            writer.newLine();

            // ---- High-level Summary ----
            double income = transactionService.getTotalIncome(yearMonth);
            double expense = transactionService.getTotalExpense(yearMonth);
            double net = income - expense;

            writer.write("=== FINANCIAL SNAPSHOT ===");
            writer.newLine();
            writer.write("Total Income,₹" + String.format("%.2f", income));
            writer.newLine();
            writer.write("Total Expense,₹" + String.format("%.2f", expense));
            writer.newLine();
            writer.write("Net Balance,₹" + String.format("%.2f", net));
            writer.newLine();
            writer.newLine();

            // ---- Category Breakdown ----
            writer.write("=== EXPENSE BY CATEGORY ===");
            writer.newLine();
            writer.write("Category,Amount (₹),Percentage");
            writer.newLine();

            Map<String, Double> expenses = categoryAggregator.aggregateExpenses(yearMonth);
            Map<String, Double> percentages = categoryAggregator.getCategoryPercentages(yearMonth);

            for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                String cat = entry.getKey();
                double amt = entry.getValue();
                double pct = percentages.getOrDefault(cat, 0.0);
                writer.write(String.format("%s,%.2f,%.1f%%", cat, amt, pct));
                writer.newLine();
            }
            writer.newLine();

            // ---- Detailed Transactions ----
            writer.write("=== DETAILED TRANSACTIONS ===");
            writer.newLine();
            writer.write("ID,Date,Type,Category,Amount (₹),Description");
            writer.newLine();

            List<Transaction> txs = transactionService.getTransactionsForMonth(yearMonth);
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            for (Transaction t : txs) {
                writer.write(String.format("%s,%s,%s,%s,%.2f,\"%s\"",
                        t.getId(),
                        t.getDate().format(df),
                        t.getType(),
                        t.getCategory(),
                        t.getAmount(),
                        t.getDescription().replace("\"", "\"\"")));
                writer.newLine();
            }
        }

        return filePath;
    }
}
