package com.example.transactions.exception;

/**
 * Excepción lanzada cuando se intenta asignar un parentId inválido o que crea ciclos.
 */
public class InvalidParentException extends RuntimeException {
    public InvalidParentException(String message) {
        super(message);
    }

    public InvalidParentException(String message, Throwable cause) {
        super(message, cause);
    }
}
