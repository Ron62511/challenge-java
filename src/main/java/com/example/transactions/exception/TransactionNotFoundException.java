package com.example.transactions.exception;

/**
 * Excepción lanzada cuando una transacción no se encuentra.
 */
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
