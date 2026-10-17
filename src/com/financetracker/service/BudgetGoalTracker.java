package com.financetracker.service;

import com.financetracker.exception.BudgetExceededException;

import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Budget Goal Tracker Module
 * --------------------------
 * Allows the user to set monthly category-wise budget limits
 * and checks whether actual spending has exceeded them.
 */
public class BudgetGoalTracker {

    private final Map<String, Double> categoryBudgets = new HashMap<>();
    private final TransactionService transactionService;
    private final CategoryAggregator categoryAggregator;

    public BudgetGoalTracker(TransactionService transactionService, CategoryAggregator categoryAggregator) {
        this.transactionService = transactionService;
        this.categoryAggregator = categoryAggregator;
    }

    public void setBudget(String category, double limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Budget limit must be positive");
        }
        categoryBudgets.put(category.trim(), limit);
    }

    public void removeBudget(String category) {
        categoryBudgets.remove(category.trim());
    }

    public Map<String, Double> getAllBudgets() {
        return Collections.unmodifiableMap(categoryBudgets);
    }

    /**
     * Checks all budgets against actual spending for the given month.
     * Throws BudgetExceededException for the first category that is over limit
     * (or you can collect all of them – this version throws on first).
     */
    public void checkBudgets(YearMonth yearMonth) throws BudgetExceededException {
        Map<String, Double> actual = categoryAggregator.aggregateExpenses(yearMonth);

        for (Map.Entry<String, Double> budget : categoryBudgets.entrySet()) {
            String cat = budget.getKey();
            double limit = budget.getValue();
            double spent = actual.getOrDefault(cat, 0.0);

            if (spent > limit) {
                throw new BudgetExceededException(cat, limit, spent);
            }
        }
    }

    /**
     * Soft version – returns a map of category → overspend amount (0 if under).
     */
    public Map<String, Double> getBudgetStatus(YearMonth yearMonth) {
        Map<String, Double> actual = categoryAggregator.aggregateExpenses(yearMonth);
        Map<String, Double> status = new HashMap<>();

        for (Map.Entry<String, Double> budget : categoryBudgets.entrySet()) {
            double spent = actual.getOrDefault(budget.getKey(), 0.0);
            status.put(budget.getKey(), Math.max(0, spent - budget.getValue()));
        }
        return status;
    }

    public void printBudgetReport(YearMonth yearMonth) {
        if (categoryBudgets.isEmpty()) {
            System.out.println("  No budgets have been set yet.");
            return;
        }

        Map<String, Double> actual = categoryAggregator.aggregateExpenses(yearMonth);

        System.out.println("\n  ╔══════════════════════════════════════════════════════════════════╗");
        System.out.printf("  ║              BUDGET STATUS – %s                           ║%n", yearMonth);
        System.out.println("  ╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("  ║ %-18s %12s %12s %12s ║%n", "Category", "Budget", "Spent", "Status");
        System.out.println("  ╠══════════════════════════════════════════════════════════════════╣");

        categoryBudgets.forEach((cat, limit) -> {
            double spent = actual.getOrDefault(cat, 0.0);
            String status;
            if (spent > limit) {
                status = "⚠ OVER";
            } else if (spent > limit * 0.8) {
                status = "⚠ Near";
            } else {
                status = "✓ OK";
            }
            System.out.printf("  ║ %-18s %12.2f %12.2f %12s ║%n", cat, limit, spent, status);
        });

        System.out.println("  ╚══════════════════════════════════════════════════════════════════╝");
    }
}
