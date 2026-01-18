package com.example.transactions.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TransactionRequest.
 * 
 * Incluye tests de validaciones (@NotNull, @Positive).
 */
class TransactionRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDefaultConstructor() {
        // Act
        TransactionRequest request = new TransactionRequest();

        // Assert
        assertNotNull(request);
        assertNull(request.getAmount());
        assertNull(request.getType());
        assertNull(request.getParentId());
    }

    @Test
    void testConstructorWithAllFields() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.0");
        String type = "cars";
        Long parentId = 1L;

        // Act
        TransactionRequest request = new TransactionRequest(amount, type, parentId);

        // Assert
        assertNotNull(request);
        assertEquals(amount, request.getAmount());
        assertEquals(type, request.getType());
        assertEquals(parentId, request.getParentId());
    }

    @Test
    void testConstructorWithoutParentId() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.0");
        String type = "cars";

        // Act
        TransactionRequest request = new TransactionRequest(amount, type, null);

        // Assert
        assertNotNull(request);
        assertEquals(amount, request.getAmount());
        assertEquals(type, request.getType());
        assertNull(request.getParentId());
    }

    @Test
    void testGetAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("5000.0");
        TransactionRequest request = new TransactionRequest(amount, "cars", null);

        // Act
        BigDecimal result = request.getAmount();

        // Assert
        assertEquals(amount, result);
    }

    @Test
    void testSetAmount() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        BigDecimal amount = new BigDecimal("100.0");

        // Act
        request.setAmount(amount);

        // Assert
        assertEquals(amount, request.getAmount());
    }

    @Test
    void testGetType() {
        // Arrange
        String type = "shopping";
        TransactionRequest request = new TransactionRequest(new BigDecimal("50.0"), type, null);

        // Act
        String result = request.getType();

        // Assert
        assertEquals(type, result);
    }

    @Test
    void testSetType() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        String type = "cars";

        // Act
        request.setType(type);

        // Assert
        assertEquals(type, request.getType());
    }

    @Test
    void testGetParentId() {
        // Arrange
        Long parentId = 10L;
        TransactionRequest request = new TransactionRequest(new BigDecimal("100.0"), "cars", parentId);

        // Act
        Long result = request.getParentId();

        // Assert
        assertEquals(parentId, result);
    }

    @Test
    void testSetParentId() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        Long parentId = 1L;

        // Act
        request.setParentId(parentId);

        // Assert
        assertEquals(parentId, request.getParentId());
    }

    @Test
    void testSetParentId_Null() {
        // Arrange
        TransactionRequest request = new TransactionRequest(new BigDecimal("100.0"), "cars", 1L);

        // Act
        request.setParentId(null);

        // Assert
        assertNull(request.getParentId());
    }

    // ========== Tests de Validaciones ==========

    @Test
    void testValidation_ValidRequest() {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.0"),
                "cars",
                null
        );

        // Act
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_AmountNull_ViolatesNotNull() {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                null,  // amount es null - viola @NotNull
                "cars",
                null
        );

        // Act
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TransactionRequest> violation = violations.iterator().next();
        assertEquals("amount", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("requerido"));
    }

    @Test
    void testValidation_AmountNegative_ViolatesPositive() {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("-10.0"),  // amount negativo - viola @Positive
                "cars",
                null
        );

        // Act
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TransactionRequest> violation = violations.iterator().next();
        assertEquals("amount", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("positivo"));
    }

    @Test
    void testValidation_AmountZero_ViolatesPositive() {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                BigDecimal.ZERO,  // amount es cero - viola @Positive
                "cars",
                null
        );

        // Act
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TransactionRequest> violation = violations.iterator().next();
        assertEquals("amount", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("positivo"));
    }

    @Test
    void testValidation_TypeNull_ViolatesNotNull() {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.0"),
                null,  // type es null - viola @NotNull
                null
        );

        // Act
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TransactionRequest> violation = violations.iterator().next();
        assertEquals("type", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("requerido"));
    }

    @Test
    void testValidation_MultipleViolations() {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                null,  // amount es null
                null,  // type es null
                null
        );

        // Act
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        // Assert
        assertEquals(2, violations.size());
    }

    @Test
    void testValidation_ParentIdOptional_NoValidation() {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.0"),
                "cars",
                null  // parentId es opcional, no tiene validaciones
        );

        // Act
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_WithValidParentId() {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.0"),
                "cars",
                10L  // parentId es válido
        );

        // Act
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }
}
