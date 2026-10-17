package com.financetracker.service;

import com.financetracker.model.Transaction;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Category Aggregator Module
 * --------------------------
 * Uses HashMap + Stream API to produce rich spending analytics.
 */
public class CategoryAggregator {

    private final TransactionService transactionService;

    public CategoryAggregator(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Returns a complete breakdown of expenses by category for the given month.
     * The map is sorted by amount descending.
     */
    public LinkedHashMap<String, Double> aggregateExpenses(YearMonth yearMonth) {
        Map<String, Double> raw = transactionService.getExpenseByCategory(yearMonth);

        return raw.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * Percentage contribution of each category to total expense.
     */
    public Map<String, Double> getCategoryPercentages(YearMonth yearMonth) {
        Map<String, Double> expenses = aggregateExpenses(yearMonth);
        double total = expenses.values().stream().mapToDouble(Double::doubleValue).sum();

        if (total == 0) {
            return Collections.emptyMap();
        }

        return expenses.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (e.getValue() / total) * 100.0,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * Pretty console report of category spending.
     */
    public void printCategoryReport(YearMonth yearMonth) {
        Map<String, Double> expenses = aggregateExpenses(yearMonth);
        Map<String, Double> percentages = getCategoryPercentages(yearMonth);

        if (expenses.isEmpty()) {
            System.out.println("  No expenses recorded for " + yearMonth);
            return;
        }

        System.out.println("\n  ╔════════════════════════════════════════════════════════════╗");
        System.out.printf("  ║          CATEGORY BREAKDOWN – %s                     ║%n", yearMonth);
        System.out.println("  ╠════════════════════════════════════════════════════════════╣");
        System.out.printf("  ║ %-20s %12s %12s ║%n", "Category", "Amount (₹)", "% of Total");
        System.out.println("  ╠════════════════════════════════════════════════════════════╣");

        expenses.forEach((cat, amount) -> {
            double pct = percentages.getOrDefault(cat, 0.0);
            System.out.printf("  ║ %-20s %12.2f %11.1f%% ║%n", cat, amount, pct);
        });

        double total = expenses.values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("  ╠════════════════════════════════════════════════════════════╣");
        System.out.printf("  ║ %-20s %12.2f %12s ║%n", "TOTAL", total, "100%");
        System.out.println("  ╚════════════════════════════════════════════════════════════╝");
    }
}
