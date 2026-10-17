package com.financetracker;

import com.financetracker.exception.BudgetExceededException;
import com.financetracker.exception.InvalidTransactionException;
import com.financetracker.export.CsvExportEngine;
import com.financetracker.model.Transaction;
import com.financetracker.model.Transaction.TransactionType;
import com.financetracker.service.BudgetGoalTracker;
import com.financetracker.service.CategoryAggregator;
import com.financetracker.service.TransactionService;
import com.financetracker.util.DateUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Personal Finance Tracker – Main Application
 * ===========================================
 * A complete console-based application demonstrating:
 *   • Stream API (filter, map, collect, groupingBy, summingDouble…)
 *   • HashMap-based aggregation
 *   • Custom exception handling
 *   • File I/O & CSV generation
 *   • Clean modular design
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private final TransactionService transactionService;
    private final CategoryAggregator categoryAggregator;
    private final BudgetGoalTracker budgetGoalTracker;
    private final CsvExportEngine csvExportEngine;

    public Main() {
        // Relative paths work both when running from IDE and from project root
        String dataDir = "data";
        String exportDir = "exports";

        this.transactionService = new TransactionService(dataDir);
        this.categoryAggregator = new CategoryAggregator(transactionService);
        this.budgetGoalTracker = new BudgetGoalTracker(transactionService, categoryAggregator);
        this.csvExportEngine = new CsvExportEngine(transactionService, categoryAggregator, exportDir);
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        printWelcome();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addTransaction();
                    case "2" -> viewAllTransactions();
                    case "3" -> viewMonthlySummary();
                    case "4" -> viewCategoryBreakdown();
                    case "5" -> manageBudgets();
                    case "6" -> exportCsv();
                    case "7" -> searchTransactions();
                    case "8" -> deleteTransaction();
                    case "0" -> {
                        System.out.println("\n  Thank you for using Personal Finance Tracker. Stay financially healthy! 💰");
                        running = false;
                    }
                    default -> System.out.println("  ⚠ Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("  ❌ Error: " + e.getMessage());
            }

            if (running) {
                System.out.println("\n  Press Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    // ------------------------------------------------------------------
    // Menu Actions
    // ------------------------------------------------------------------

    private void addTransaction() {
        System.out.println("\n  ── Add New Transaction ──");

        LocalDate date = readDate("  Date (dd-MM-yyyy) or press Enter for today: ");
        if (date == null) date = LocalDate.now();

        System.out.print("  Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("  Category (e.g. Food, Salary, Rent, Transport): ");
        String category = scanner.nextLine().trim();

        System.out.print("  Amount (₹): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  ❌ Invalid amount.");
            return;
        }

        System.out.print("  Type (I = Income / E = Expense): ");
        String typeInput = scanner.nextLine().trim().toUpperCase();
        TransactionType type = typeInput.startsWith("I") ? TransactionType.INCOME : TransactionType.EXPENSE;

        try {
            Transaction tx = transactionService.addTransaction(date, description, category, amount, type);
            System.out.println("\n  ✅ Transaction added successfully!");
            System.out.println("  " + tx);

            // Soft budget check for expenses
            if (type == TransactionType.EXPENSE) {
                try {
                    budgetGoalTracker.checkBudgets(YearMonth.from(date));
                } catch (BudgetExceededException be) {
                    System.out.println("\n  ⚠ WARNING: " + be.getMessage());
                }
            }
        } catch (InvalidTransactionException e) {
            System.out.println("  ❌ " + e.getMessage());
        }
    }

    private void viewAllTransactions() {
        List<Transaction> all = transactionService.getAllTransactions();
        if (all.isEmpty()) {
            System.out.println("\n  No transactions recorded yet.");
            return;
        }

        System.out.println("\n  ── All Transactions (" + all.size() + ") ──");
        System.out.println("  " + "-".repeat(85));
        all.forEach(t -> System.out.println("  " + t));
        System.out.println("  " + "-".repeat(85));
    }

    private void viewMonthlySummary() {
        YearMonth ym = readYearMonth();
        if (ym == null) return;

        double income = transactionService.getTotalIncome(ym);
        double expense = transactionService.getTotalExpense(ym);
        double net = income - expense;

        System.out.println("\n  ╔════════════════════════════════════════════╗");
        System.out.printf("  ║       MONTHLY SUMMARY – %s            ║%n", ym);
        System.out.println("  ╠════════════════════════════════════════════╣");
        System.out.printf("  ║  Total Income   : ₹ %12.2f          ║%n", income);
        System.out.printf("  ║  Total Expense  : ₹ %12.2f          ║%n", expense);
        System.out.println("  ╠════════════════════════════════════════════╣");
        System.out.printf("  ║  Net Balance    : ₹ %12.2f          ║%n", net);
        System.out.println("  ╚════════════════════════════════════════════╝");

        // Quick top categories
        List<Map.Entry<String, Double>> top = transactionService.getTopSpendingCategories(ym, 3);
        if (!top.isEmpty()) {
            System.out.println("\n  Top Spending Categories:");
            top.forEach(e -> System.out.printf("    • %-15s ₹ %.2f%n", e.getKey(), e.getValue()));
        }
    }

    private void viewCategoryBreakdown() {
        YearMonth ym = readYearMonth();
        if (ym == null) return;
        categoryAggregator.printCategoryReport(ym);
    }

    private void manageBudgets() {
        System.out.println("\n  ── Budget Goal Tracker ──");
        System.out.println("  1. Set / Update a category budget");
        System.out.println("  2. View current budgets & status");
        System.out.println("  3. Remove a budget");
        System.out.print("  Choice: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.print("  Category name: ");
                String cat = scanner.nextLine().trim();
                System.out.print("  Monthly budget limit (₹): ");
                try {
                    double limit = Double.parseDouble(scanner.nextLine().trim());
                    budgetGoalTracker.setBudget(cat, limit);
                    System.out.println("  ✅ Budget set for " + cat);
                } catch (NumberFormatException e) {
                    System.out.println("  ❌ Invalid number.");
                }
            }
            case "2" -> {
                YearMonth ym = YearMonth.now();
                budgetGoalTracker.printBudgetReport(ym);
            }
            case "3" -> {
                System.out.print("  Category to remove: ");
                String cat = scanner.nextLine().trim();
                budgetGoalTracker.removeBudget(cat);
                System.out.println("  ✅ Budget removed (if it existed).");
            }
            default -> System.out.println("  Invalid choice.");
        }
    }

    private void exportCsv() {
        YearMonth ym = readYearMonth();
        if (ym == null) return;

        try {
            Path path = csvExportEngine.exportMonthlySummary(ym);
            System.out.println("\n  ✅ Monthly report exported successfully!");
            System.out.println("  📁 Location: " + path.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("  ❌ Export failed: " + e.getMessage());
        }
    }

    private void searchTransactions() {
        System.out.print("\n  Enter keyword to search in description: ");
        String keyword = scanner.nextLine().trim();
        List<Transaction> results = transactionService.searchByDescription(keyword);

        if (results.isEmpty()) {
            System.out.println("  No matching transactions found.");
        } else {
            System.out.println("\n  Found " + results.size() + " transaction(s):");
            results.forEach(t -> System.out.println("  " + t));
        }
    }

    private void deleteTransaction() {
        System.out.print("\n  Enter Transaction ID to delete: ");
        String id = scanner.nextLine().trim();
        if (transactionService.removeTransaction(id)) {
            System.out.println("  ✅ Transaction deleted.");
        } else {
            System.out.println("  ❌ Transaction not found.");
        }
    }

    // ------------------------------------------------------------------
    // Helper input methods
    // ------------------------------------------------------------------

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;

        Optional<LocalDate> date = DateUtils.parseDate(input);
        if (date.isEmpty()) {
            System.out.println("  ❌ Invalid date format. Use dd-MM-yyyy");
            return null;
        }
        return date.get();
    }

    private YearMonth readYearMonth() {
        System.out.print("  Enter month (yyyy-MM) or press Enter for current month: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(input);
        } catch (Exception e) {
            System.out.println("  ❌ Invalid format. Use yyyy-MM (e.g. 2026-10)");
            return null;
        }
    }

    private void printWelcome() {
        System.out.println("""
                
                  ██████╗ ███████╗██████╗ ███████╗ ██████╗ ███╗   ██╗ █████╗ ██╗     
                  ██╔══██╗██╔════╝██╔══██╗██╔════╝██╔═══██╗████╗  ██║██╔══██╗██║     
                  ██████╔╝█████╗  ██████╔╝███████╗██║   ██║██╔██╗ ██║███████║██║     
                  ██╔═══╝ ██╔══╝  ██╔══██╗╚════██║██║   ██║██║╚██╗██║██╔══██║██║     
                  ██║     ███████╗██║  ██║███████║╚██████╔╝██║ ╚████║██║  ██║███████╗
                  ╚═╝     ╚══════╝╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝
                  
                       PERSONAL FINANCE TRACKER  •  Java Streams Edition
                """);
    }

    private void printMenu() {
        System.out.println("""
                
                  ┌─────────────────────────────────────────────┐
                  │           MAIN MENU                         │
                  ├─────────────────────────────────────────────┤
                  │  1. Add Income / Expense                    │
                  │  2. View All Transactions                   │
                  │  3. Monthly Financial Summary               │
                  │  4. Category-wise Spending Analysis         │
                  │  5. Budget Goal Tracker                     │
                  │  6. Export Monthly Summary (CSV)            │
                  │  7. Search Transactions                     │
                  │  8. Delete a Transaction                    │
                  │  0. Exit                                    │
                  └─────────────────────────────────────────────┘
                  Enter choice: """);
    }
}
