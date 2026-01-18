package com.example.transactions.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para InvalidParentException.
 */
class InvalidParentExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String message = "La transacción padre con ID 999 no existe";

        // Act
        InvalidParentException exception = new InvalidParentException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Arrange
        String message = "Parent inválido";
        Throwable cause = new IllegalArgumentException("Causa original");

        // Act
        InvalidParentException exception = new InvalidParentException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithNullMessage() {
        // Act
        InvalidParentException exception = new InvalidParentException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act
        InvalidParentException exception = new InvalidParentException(emptyMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testInheritance() {
        // Arrange & Act
        InvalidParentException exception = new InvalidParentException("test");

        // Assert
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void testGetCause() {
        // Arrange
        Throwable cause = new IllegalStateException("Causa");
        InvalidParentException exception = new InvalidParentException("mensaje", cause);

        // Act
        Throwable result = exception.getCause();

        // Assert
        assertEquals(cause, result);
        assertEquals("Causa", result.getMessage());
    }

    @Test
    void testMessage_CycleDetection() {
        // Arrange
        String message = "Asignar parentId 2 a la transacción 1 crearía un ciclo en la jerarquía";

        // Act
        InvalidParentException exception = new InvalidParentException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("ciclo"));
    }

    @Test
    void testMessage_ParentNotFound() {
        // Arrange
        String message = "La transacción padre con ID 999 no existe";

        // Act
        InvalidParentException exception = new InvalidParentException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("no existe"));
    }
}
