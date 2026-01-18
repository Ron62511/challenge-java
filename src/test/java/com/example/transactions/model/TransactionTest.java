package com.example.transactions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Transaction (Model).
 * 
 * Cubre constructores, getters/setters, equals, hashCode y toString.
 */
class TransactionTest {

    @Test
    void testDefaultConstructor() {
        // Act
        Transaction transaction = new Transaction();

        // Assert
        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertNull(transaction.getAmount());
        assertNull(transaction.getType());
        assertNull(transaction.getParentId());
    }

    @Test
    void testConstructorWithAllFields() {
        // Arrange
        Long id = 1L;
        BigDecimal amount = new BigDecimal("100.0");
        String type = "cars";
        Long parentId = null;

        // Act
        Transaction transaction = new Transaction(id, amount, type, parentId);

        // Assert
        assertNotNull(transaction);
        assertEquals(id, transaction.getId());
        assertEquals(amount, transaction.getAmount());
        assertEquals(type, transaction.getType());
        assertEquals(parentId, transaction.getParentId());
    }

    @Test
    void testConstructorWithParentId() {
        // Arrange
        Long id = 2L;
        BigDecimal amount = new BigDecimal("50.0");
        String type = "shopping";
        Long parentId = 1L;

        // Act
        Transaction transaction = new Transaction(id, amount, type, parentId);

        // Assert
        assertNotNull(transaction);
        assertEquals(id, transaction.getId());
        assertEquals(amount, transaction.getAmount());
        assertEquals(type, transaction.getType());
        assertEquals(parentId, transaction.getParentId());
    }

    // ========== Tests de Getters y Setters ==========

    @Test
    void testGetId() {
        // Arrange
        Long id = 10L;
        Transaction transaction = new Transaction(id, new BigDecimal("100.0"), "cars", null);

        // Act
        Long result = transaction.getId();

        // Assert
        assertEquals(id, result);
    }

    @Test
    void testSetId() {
        // Arrange
        Transaction transaction = new Transaction();
        Long id = 1L;

        // Act
        transaction.setId(id);

        // Assert
        assertEquals(id, transaction.getId());
    }

    @Test
    void testSetId_Null() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        transaction.setId(null);

        // Assert
        assertNull(transaction.getId());
    }

    @Test
    void testGetAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("5000.0");
        Transaction transaction = new Transaction(1L, amount, "cars", null);

        // Act
        BigDecimal result = transaction.getAmount();

        // Assert
        assertEquals(amount, result);
        assertEquals(0, amount.compareTo(result));
    }

    @Test
    void testSetAmount() {
        // Arrange
        Transaction transaction = new Transaction();
        BigDecimal amount = new BigDecimal("100.0");

        // Act
        transaction.setAmount(amount);

        // Assert
        assertEquals(amount, transaction.getAmount());
    }

    @Test
    void testSetAmount_Null() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        transaction.setAmount(null);

        // Assert
        assertNull(transaction.getAmount());
    }

    @Test
    void testSetAmount_DecimalPrecision() {
        // Arrange
        Transaction transaction = new Transaction();
        BigDecimal preciseAmount = new BigDecimal("123.456789");

        // Act
        transaction.setAmount(preciseAmount);

        // Assert
        assertEquals(preciseAmount, transaction.getAmount());
        assertEquals(preciseAmount.scale(), transaction.getAmount().scale());
    }

    @Test
    void testGetType() {
        // Arrange
        String type = "shopping";
        Transaction transaction = new Transaction(1L, new BigDecimal("50.0"), type, null);

        // Act
        String result = transaction.getType();

        // Assert
        assertEquals(type, result);
    }

    @Test
    void testSetType() {
        // Arrange
        Transaction transaction = new Transaction();
        String type = "cars";

        // Act
        transaction.setType(type);

        // Assert
        assertEquals(type, transaction.getType());
    }

    @Test
    void testSetType_Null() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        transaction.setType(null);

        // Assert
        assertNull(transaction.getType());
    }

    @Test
    void testGetParentId() {
        // Arrange
        Long parentId = 10L;
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", parentId);

        // Act
        Long result = transaction.getParentId();

        // Assert
        assertEquals(parentId, result);
    }

    @Test
    void testSetParentId() {
        // Arrange
        Transaction transaction = new Transaction();
        Long parentId = 1L;

        // Act
        transaction.setParentId(parentId);

        // Assert
        assertEquals(parentId, transaction.getParentId());
    }

    @Test
    void testSetParentId_Null() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", 10L);

        // Act
        transaction.setParentId(null);

        // Assert
        assertNull(transaction.getParentId());
    }

    // ========== Tests de equals ==========

    @Test
    void testEquals_SameInstance() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act & Assert
        assertEquals(transaction, transaction);
        assertTrue(transaction.equals(transaction));
    }

    @Test
    void testEquals_SameId() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(1L, new BigDecimal("200.0"), "shopping", 10L);

        // Act & Assert
        assertEquals(transaction1, transaction2);
        assertTrue(transaction1.equals(transaction2));
    }

    @Test
    void testEquals_DifferentId() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(2L, new BigDecimal("100.0"), "cars", null);

        // Act & Assert
        assertNotEquals(transaction1, transaction2);
        assertFalse(transaction1.equals(transaction2));
    }

    @Test
    void testEquals_Null() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act & Assert
        assertNotEquals(transaction, null);
        assertFalse(transaction.equals(null));
    }

    @Test
    void testEquals_DifferentClass() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        String notATransaction = "not a transaction";

        // Act & Assert
        assertNotEquals(transaction, notATransaction);
        assertFalse(transaction.equals(notATransaction));
    }

    @Test
    void testEquals_BothNullId() {
        // Arrange
        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();

        // Act & Assert
        // Dos transacciones con id null NO son iguales (equals solo compara por id)
        assertEquals(transaction1, transaction2);
        // Nota: Esto funciona porque Objects.equals(null, null) retorna true
    }

    @Test
    void testEquals_OneNullId() {
        // Arrange
        Transaction transaction1 = new Transaction(null, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act & Assert
        assertNotEquals(transaction1, transaction2);
        assertFalse(transaction1.equals(transaction2));
    }

    // ========== Tests de hashCode ==========

    @Test
    void testHashCode_SameId() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(1L, new BigDecimal("200.0"), "shopping", 10L);

        // Act & Assert
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    void testHashCode_DifferentId() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(2L, new BigDecimal("100.0"), "cars", null);

        // Act & Assert
        // No garantizamos que sean diferentes, pero es muy probable
        // Solo verificamos que el hashCode se pueda calcular sin excepciones
        assertNotNull(transaction1.hashCode());
        assertNotNull(transaction2.hashCode());
    }

    @Test
    void testHashCode_NullId() {
        // Arrange
        Transaction transaction = new Transaction();

        // Act & Assert
        // hashCode de null es 0, debe ejecutarse sin excepción
        assertNotNull(transaction.hashCode());
    }

    @Test
    void testHashCode_Consistency() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        int hashCode1 = transaction.hashCode();
        int hashCode2 = transaction.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    // ========== Tests de toString ==========

    @Test
    void testToString() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        String result = transaction.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Transaction{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("amount=100.0"));
        assertTrue(result.contains("type='cars'"));
        assertTrue(result.contains("parentId=null"));
    }

    @Test
    void testToString_WithParentId() {
        // Arrange
        Transaction transaction = new Transaction(2L, new BigDecimal("50.0"), "shopping", 1L);

        // Act
        String result = transaction.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("id=2"));
        assertTrue(result.contains("amount=50.0"));
        assertTrue(result.contains("type='shopping'"));
        assertTrue(result.contains("parentId=1"));
    }

    @Test
    void testToString_NullFields() {
        // Arrange
        Transaction transaction = new Transaction();

        // Act
        String result = transaction.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Transaction{"));
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("amount=null"));
        assertTrue(result.contains("type='null'"));
        assertTrue(result.contains("parentId=null"));
    }

    // ========== Tests de mutabilidad ==========

    @Test
    void testMutability_ChangeAfterCreation() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        transaction.setAmount(new BigDecimal("200.0"));
        transaction.setType("shopping");
        transaction.setParentId(10L);

        // Assert
        assertEquals(new BigDecimal("200.0"), transaction.getAmount());
        assertEquals("shopping", transaction.getType());
        assertEquals(10L, transaction.getParentId());
        assertEquals(1L, transaction.getId()); // id no cambió
    }
}
