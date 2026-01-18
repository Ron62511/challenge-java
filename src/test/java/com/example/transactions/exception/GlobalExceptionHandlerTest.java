package com.example.transactions.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para GlobalExceptionHandler.
 * 
 * Verifica que las excepciones se mapeen correctamente a códigos HTTP
 * y respuestas adecuadas.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // ========== Tests para TransactionNotFoundException ==========

    @Test
    void testHandleTransactionNotFoundException() {
        // Arrange
        String message = "Transacción con ID 999 no encontrada";
        TransactionNotFoundException exception = new TransactionNotFoundException(message);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleTransactionNotFoundException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().get("error"));
    }

    @Test
    void testHandleTransactionNotFoundException_WithNullMessage() {
        // Arrange
        TransactionNotFoundException exception = new TransactionNotFoundException(null);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleTransactionNotFoundException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().get("error"));
    }

    // ========== Tests para DuplicateTransactionException ==========

    @Test
    void testHandleDuplicateTransactionException() {
        // Arrange
        String message = "Transacción con ID 10 ya existe";
        DuplicateTransactionException exception = new DuplicateTransactionException(message);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDuplicateTransactionException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().get("error"));
    }

    @Test
    void testHandleDuplicateTransactionException_WithNullMessage() {
        // Arrange
        DuplicateTransactionException exception = new DuplicateTransactionException(null);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDuplicateTransactionException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().get("error"));
    }

    // ========== Tests para InvalidParentException ==========

    @Test
    void testHandleInvalidParentException() {
        // Arrange
        String message = "La transacción padre con ID 999 no existe";
        InvalidParentException exception = new InvalidParentException(message);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleInvalidParentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().get("error"));
    }

    @Test
    void testHandleInvalidParentException_CycleMessage() {
        // Arrange
        String message = "Asignar parentId 2 a la transacción 1 crearía un ciclo";
        InvalidParentException exception = new InvalidParentException(message);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleInvalidParentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().get("error"));
    }

    @Test
    void testHandleInvalidParentException_WithNullMessage() {
        // Arrange
        InvalidParentException exception = new InvalidParentException(null);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleInvalidParentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().get("error"));
    }

    // ========== Tests para MethodArgumentNotValidException ==========

    @Test
    void testHandleValidationException_SingleFieldError() {
        // Arrange
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "transactionRequest");
        bindingResult.addError(new FieldError("transactionRequest", "amount", "amount es requerido"));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("amount es requerido", response.getBody().get("amount"));
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testHandleValidationException_MultipleFieldErrors() {
        // Arrange
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "transactionRequest");
        bindingResult.addError(new FieldError("transactionRequest", "amount", "amount es requerido"));
        bindingResult.addError(new FieldError("transactionRequest", "type", "type es requerido"));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("amount es requerido", response.getBody().get("amount"));
        assertEquals("type es requerido", response.getBody().get("type"));
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testHandleValidationException_EmptyFieldErrors() {
        // Arrange
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "transactionRequest");
        // No agregar errores - lista vacía
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ========== Tests para Exception genérica ==========

    @Test
    void testHandleGenericException() {
        // Arrange
        String message = "Error inesperado";
        Exception exception = new Exception(message);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error interno del servidor"));
        assertTrue(response.getBody().get("error").contains(message));
    }

    @Test
    void testHandleGenericException_WithNullMessage() {
        // Arrange
        Exception exception = new Exception((String) null);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error interno del servidor"));
    }

    @Test
    void testHandleGenericException_RuntimeException() {
        // Arrange
        String message = "Runtime error";
        RuntimeException exception = new RuntimeException(message);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error interno del servidor"));
        assertTrue(response.getBody().get("error").contains(message));
    }

    @Test
    void testHandleGenericException_IllegalArgumentException() {
        // Arrange
        String message = "Argumento inválido";
        IllegalArgumentException exception = new IllegalArgumentException(message);

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error interno del servidor"));
    }

    // ========== Tests de formato de respuesta ==========

    @Test
    void testResponseFormat_AllHandlers() {
        // Arrange
        String message = "Mensaje de error";

        // Act
        ResponseEntity<Map<String, String>> response1 = 
            exceptionHandler.handleTransactionNotFoundException(new TransactionNotFoundException(message));
        ResponseEntity<Map<String, String>> response2 = 
            exceptionHandler.handleDuplicateTransactionException(new DuplicateTransactionException(message));
        ResponseEntity<Map<String, String>> response3 = 
            exceptionHandler.handleInvalidParentException(new InvalidParentException(message));

        // Assert - Todas las respuestas tienen el formato correcto
        assertTrue(response1.getBody().containsKey("error"));
        assertTrue(response2.getBody().containsKey("error"));
        assertTrue(response3.getBody().containsKey("error"));
        
        assertEquals(message, response1.getBody().get("error"));
        assertEquals(message, response2.getBody().get("error"));
        assertEquals(message, response3.getBody().get("error"));
    }

    @Test
    void testHttpStatusCodes() {
        // Arrange
        String message = "Test message";

        // Act & Assert
        ResponseEntity<Map<String, String>> notFoundResponse = 
            exceptionHandler.handleTransactionNotFoundException(new TransactionNotFoundException(message));
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());

        ResponseEntity<Map<String, String>> conflictResponse = 
            exceptionHandler.handleDuplicateTransactionException(new DuplicateTransactionException(message));
        assertEquals(HttpStatus.CONFLICT, conflictResponse.getStatusCode());

        ResponseEntity<Map<String, String>> badRequestResponse = 
            exceptionHandler.handleInvalidParentException(new InvalidParentException(message));
        assertEquals(HttpStatus.BAD_REQUEST, badRequestResponse.getStatusCode());

        ResponseEntity<Map<String, String>> internalErrorResponse = 
            exceptionHandler.handleGenericException(new Exception(message));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, internalErrorResponse.getStatusCode());
    }
}
