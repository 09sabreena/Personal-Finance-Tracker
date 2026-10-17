package com.financetracker.exception;

/**
 * Thrown when a user tries to create an invalid transaction
 * (negative amount, empty description, future date beyond limit, etc.)
 */
public class InvalidTransactionException extends Exception {

    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
