package com.financetracker.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable Transaction model representing a single income or expense entry.
 * Implements Serializable so the entire list can be persisted easily.
 */
public final class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final String id;
    private final LocalDate date;
    private final String description;
    private final String category;
    private final double amount;          // positive = income, negative = expense
    private final TransactionType type;

    public enum TransactionType {
        INCOME, EXPENSE
    }

    public Transaction(LocalDate date, String description, String category, double amount, TransactionType type) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null").trim();
        this.category = Objects.requireNonNull(category, "Category cannot be null").trim();
        this.amount = amount;
        this.type = Objects.requireNonNull(type, "Type cannot be null");

        if (description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (category.isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive (use type to indicate expense)");
        }
    }

    // Getters
    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }

    /** Signed amount: + for income, - for expense */
    public double getSignedAmount() {
        return type == TransactionType.INCOME ? amount : -amount;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-10s | %-12s | %-25s | %10.2f | %s",
                id,
                date.format(FORMATTER),
                type,
                category,
                amount,
                description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
