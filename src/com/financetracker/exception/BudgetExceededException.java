package com.financetracker.exception;

/**
 * Thrown (or used as a soft warning) when spending in a category
 * exceeds the user-defined monthly budget goal.
 */
public class BudgetExceededException extends Exception {

    private final String category;
    private final double budgetLimit;
    private final double actualSpent;

    public BudgetExceededException(String category, double budgetLimit, double actualSpent) {
        super(String.format(
                "Budget exceeded for category '%s'! Limit: ₹%.2f | Actual: ₹%.2f | Over by: ₹%.2f",
                category, budgetLimit, actualSpent, actualSpent - budgetLimit));
        this.category = category;
        this.budgetLimit = budgetLimit;
        this.actualSpent = actualSpent;
    }

    public String getCategory() { return category; }
    public double getBudgetLimit() { return budgetLimit; }
    public double getActualSpent() { return actualSpent; }
}
