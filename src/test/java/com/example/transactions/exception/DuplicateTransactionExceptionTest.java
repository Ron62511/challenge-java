package com.example.transactions.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para DuplicateTransactionException.
 */
class DuplicateTransactionExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String message = "Transacción con ID 10 ya existe";

        // Act
        DuplicateTransactionException exception = new DuplicateTransactionException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Arrange
        String message = "Transacción duplicada";
        Throwable cause = new IllegalArgumentException("Causa original");

        // Act
        DuplicateTransactionException exception = new DuplicateTransactionException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithNullMessage() {
        // Act
        DuplicateTransactionException exception = new DuplicateTransactionException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act
        DuplicateTransactionException exception = new DuplicateTransactionException(emptyMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testInheritance() {
        // Arrange & Act
        DuplicateTransactionException exception = new DuplicateTransactionException("test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testGetCause() {
        // Arrange
        Throwable cause = new IllegalStateException("Causa");
        DuplicateTransactionException exception = new DuplicateTransactionException("mensaje", cause);

        // Act
        Throwable result = exception.getCause();

        // Assert
        assertEquals(cause, result);
        assertEquals("Causa", result.getMessage());
    }
}
