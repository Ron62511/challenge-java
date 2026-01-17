package com.example.transactions.exception;

/**
 * Excepción lanzada cuando se intenta crear una transacción con un ID duplicado.
 */
public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) {
        super(message);
    }

    public DuplicateTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
