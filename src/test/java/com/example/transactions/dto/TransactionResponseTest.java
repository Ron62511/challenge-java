package com.example.transactions.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TransactionResponse.
 * 
 * Incluye tests de serialización JSON para verificar @JsonProperty("parent_id").
 */
class TransactionResponseTest {

    @Test
    void testDefaultConstructor() {
        // Act
        TransactionResponse response = new TransactionResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getAmount());
        assertNull(response.getType());
        assertNull(response.getParentId());
    }

    @Test
    void testConstructorWithAllFields() {
        // Arrange
        Long id = 1L;
        BigDecimal amount = new BigDecimal("100.0");
        String type = "cars";
        Long parentId = null;

        // Act
        TransactionResponse response = new TransactionResponse(id, amount, type, parentId);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(amount, response.getAmount());
        assertEquals(type, response.getType());
        assertEquals(parentId, response.getParentId());
    }

    @Test
    void testConstructorWithParentId() {
        // Arrange
        Long id = 2L;
        BigDecimal amount = new BigDecimal("50.0");
        String type = "shopping";
        Long parentId = 1L;

        // Act
        TransactionResponse response = new TransactionResponse(id, amount, type, parentId);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(amount, response.getAmount());
        assertEquals(type, response.getType());
        assertEquals(parentId, response.getParentId());
    }

    @Test
    void testGetId() {
        // Arrange
        Long id = 10L;
        TransactionResponse response = new TransactionResponse(id, new BigDecimal("100.0"), "cars", null);

        // Act
        Long result = response.getId();

        // Assert
        assertEquals(id, result);
    }

    @Test
    void testSetId() {
        // Arrange
        TransactionResponse response = new TransactionResponse();
        Long id = 1L;

        // Act
        response.setId(id);

        // Assert
        assertEquals(id, response.getId());
    }

    @Test
    void testSetId_Null() {
        // Arrange
        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        response.setId(null);

        // Assert
        assertNull(response.getId());
    }

    @Test
    void testGetAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("5000.0");
        TransactionResponse response = new TransactionResponse(1L, amount, "cars", null);

        // Act
        BigDecimal result = response.getAmount();

        // Assert
        assertEquals(amount, result);
    }

    @Test
    void testSetAmount() {
        // Arrange
        TransactionResponse response = new TransactionResponse();
        BigDecimal amount = new BigDecimal("100.0");

        // Act
        response.setAmount(amount);

        // Assert
        assertEquals(amount, response.getAmount());
    }

    @Test
    void testGetType() {
        // Arrange
        String type = "shopping";
        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("50.0"), type, null);

        // Act
        String result = response.getType();

        // Assert
        assertEquals(type, result);
    }

    @Test
    void testSetType() {
        // Arrange
        TransactionResponse response = new TransactionResponse();
        String type = "cars";

        // Act
        response.setType(type);

        // Assert
        assertEquals(type, response.getType());
    }

    @Test
    void testGetParentId() {
        // Arrange
        Long parentId = 10L;
        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("100.0"), "cars", parentId);

        // Act
        Long result = response.getParentId();

        // Assert
        assertEquals(parentId, result);
    }

    @Test
    void testSetParentId() {
        // Arrange
        TransactionResponse response = new TransactionResponse();
        Long parentId = 1L;

        // Act
        response.setParentId(parentId);

        // Assert
        assertEquals(parentId, response.getParentId());
    }

    @Test
    void testSetParentId_Null() {
        // Arrange
        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("100.0"), "cars", 10L);

        // Act
        response.setParentId(null);

        // Assert
        assertNull(response.getParentId());
    }

    // ========== Tests de Serialización JSON ==========

    @Test
    void testJsonSerialization_WithParentId() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("100.0"), "cars", 10L);

        // Act
        String json = mapper.writeValueAsString(response);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"amount\":100.0"));
        assertTrue(json.contains("\"type\":\"cars\""));
        assertTrue(json.contains("\"parent_id\":10"));  // Verificar que usa snake_case
    }

    @Test
    void testJsonSerialization_WithoutParentId() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        TransactionResponse response = new TransactionResponse(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        String json = mapper.writeValueAsString(response);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"amount\":100.0"));
        assertTrue(json.contains("\"type\":\"cars\""));
        assertTrue(json.contains("\"parent_id\":null") || json.contains("\"parent_id\":null"));
    }

    @Test
    void testJsonDeserialization_WithParentId() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"amount\":100.0,\"type\":\"cars\",\"parent_id\":10}";

        // Act
        TransactionResponse response = mapper.readValue(json, TransactionResponse.class);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("100.0"), response.getAmount());
        assertEquals("cars", response.getType());
        assertEquals(10L, response.getParentId());
    }

    @Test
    void testJsonDeserialization_WithoutParentId() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"amount\":100.0,\"type\":\"cars\",\"parent_id\":null}";

        // Act
        TransactionResponse response = mapper.readValue(json, TransactionResponse.class);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("100.0"), response.getAmount());
        assertEquals("cars", response.getType());
        assertNull(response.getParentId());
    }

    @Test
    void testJsonDeserialization_WithSnakeCase() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":2,\"amount\":50.0,\"type\":\"shopping\",\"parent_id\":1}";

        // Act
        TransactionResponse response = mapper.readValue(json, TransactionResponse.class);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(new BigDecimal("50.0"), response.getAmount());
        assertEquals("shopping", response.getType());
        assertEquals(1L, response.getParentId());
    }
}
