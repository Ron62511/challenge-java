package com.example.transactions.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TransactionNotFoundException.
 */
class TransactionNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String message = "Transacción con ID 999 no encontrada";

        // Act
        TransactionNotFoundException exception = new TransactionNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Arrange
        String message = "Transacción no encontrada";
        Throwable cause = new IllegalArgumentException("Causa original");

        // Act
        TransactionNotFoundException exception = new TransactionNotFoundException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithNullMessage() {
        // Act
        TransactionNotFoundException exception = new TransactionNotFoundException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act
        TransactionNotFoundException exception = new TransactionNotFoundException(emptyMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testInheritance() {
        // Arrange & Act
        TransactionNotFoundException exception = new TransactionNotFoundException("test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testGetCause() {
        // Arrange
        Throwable cause = new IllegalStateException("Causa");
        TransactionNotFoundException exception = new TransactionNotFoundException("mensaje", cause);

        // Act
        Throwable result = exception.getCause();

        // Assert
        assertEquals(cause, result);
        assertEquals("Causa", result.getMessage());
    }

    @Test
    void testMessage_WithTransactionId() {
        // Arrange
        Long id = 999L;
        String message = "Transacción con ID " + id + " no encontrada";

        // Act
        TransactionNotFoundException exception = new TransactionNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("no encontrada"));
        assertTrue(exception.getMessage().contains("999"));
    }
}
