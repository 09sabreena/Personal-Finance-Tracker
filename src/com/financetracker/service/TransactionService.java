package com.financetracker.service;

import com.financetracker.exception.InvalidTransactionException;
import com.financetracker.model.Transaction;
import com.financetracker.model.Transaction.TransactionType;
import com.financetracker.util.DateUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central service responsible for adding, removing, querying and persisting transactions.
 * Demonstrates heavy use of Stream API for filtering, mapping and aggregation.
 */
public class TransactionService {

    private final List<Transaction> transactions = new ArrayList<>();
    private final Path storagePath;

    public TransactionService(String dataFolder) {
        this.storagePath = Paths.get(dataFolder, "transactions.dat");
        loadFromDisk();
    }

    // ------------------------------------------------------------------
    // CRUD Operations
    // ------------------------------------------------------------------

    public Transaction addTransaction(LocalDate date, String description, String category,
                                      double amount, TransactionType type)
            throws InvalidTransactionException {

        validate(date, description, category, amount);

        Transaction tx = new Transaction(date, description, category, amount, type);
        transactions.add(tx);
        saveToDisk();
        return tx;
    }

    public boolean removeTransaction(String id) {
        boolean removed = transactions.removeIf(t -> t.getId().equalsIgnoreCase(id));
        if (removed) {
            saveToDisk();
        }
        return removed;
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // ------------------------------------------------------------------
    // Stream-powered Query Methods (the heart of the project)
    // ------------------------------------------------------------------

    /** All transactions belonging to a specific YearMonth */
    public List<Transaction> getTransactionsForMonth(YearMonth yearMonth) {
        return transactions.stream()
                .filter(t -> DateUtils.isInMonth(t.getDate(), yearMonth))
                .sorted(Comparator.comparing(Transaction::getDate))
                .collect(Collectors.toList());
    }

    /** All expenses (or incomes) for a given month */
    public List<Transaction> getByTypeForMonth(YearMonth yearMonth, TransactionType type) {
        return getTransactionsForMonth(yearMonth).stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    /** Total income for a month */
    public double getTotalIncome(YearMonth yearMonth) {
        return getByTypeForMonth(yearMonth, TransactionType.INCOME).stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Total expense for a month */
    public double getTotalExpense(YearMonth yearMonth) {
        return getByTypeForMonth(yearMonth, TransactionType.EXPENSE).stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Net balance (Income - Expense) */
    public double getNetBalance(YearMonth yearMonth) {
        return getTotalIncome(yearMonth) - getTotalExpense(yearMonth);
    }

    /** Group expenses by category using Collectors.groupingBy + summingDouble */
    public Map<String, Double> getExpenseByCategory(YearMonth yearMonth) {
        return getByTypeForMonth(yearMonth, TransactionType.EXPENSE).stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    /** Top N spending categories for a month (sorted descending) */
    public List<Map.Entry<String, Double>> getTopSpendingCategories(YearMonth yearMonth, int topN) {
        return getExpenseByCategory(yearMonth).entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /** Search by description keyword (case-insensitive) */
    public List<Transaction> searchByDescription(String keyword) {
        String lower = keyword.toLowerCase();
        return transactions.stream()
                .filter(t -> t.getDescription().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Persistence (simple Java serialization for demo clarity)
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void loadFromDisk() {
        if (!Files.exists(storagePath)) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storagePath.toFile()))) {
            List<Transaction> loaded = (List<Transaction>) ois.readObject();
            transactions.clear();
            transactions.addAll(loaded);
        } catch (Exception e) {
            System.err.println("⚠ Could not load previous transactions: " + e.getMessage());
        }
    }

    private void saveToDisk() {
        try {
            Files.createDirectories(storagePath.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storagePath.toFile()))) {
                oos.writeObject(new ArrayList<>(transactions));
            }
        } catch (IOException e) {
            System.err.println("⚠ Could not save transactions: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------

    private void validate(LocalDate date, String description, String category, double amount)
            throws InvalidTransactionException {

        if (date == null) {
            throw new InvalidTransactionException("Date cannot be null");
        }
        if (date.isAfter(LocalDate.now().plusDays(7))) {
            throw new InvalidTransactionException("Date cannot be more than 7 days in the future");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidTransactionException("Description cannot be empty");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidTransactionException("Category cannot be empty");
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }
    }
}
