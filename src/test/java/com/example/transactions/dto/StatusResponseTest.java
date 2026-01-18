package com.example.transactions.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para StatusResponse.
 */
class StatusResponseTest {

    @Test
    void testDefaultConstructor() {
        // Act
        StatusResponse response = new StatusResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getStatus());
    }

    @Test
    void testConstructorWithStatus() {
        // Arrange
        String status = "ok";

        // Act
        StatusResponse response = new StatusResponse(status);

        // Assert
        assertNotNull(response);
        assertEquals(status, response.getStatus());
    }

    @Test
    void testGetStatus() {
        // Arrange
        String status = "success";
        StatusResponse response = new StatusResponse(status);

        // Act
        String result = response.getStatus();

        // Assert
        assertEquals(status, result);
    }

    @Test
    void testSetStatus() {
        // Arrange
        StatusResponse response = new StatusResponse();
        String status = "ok";

        // Act
        response.setStatus(status);

        // Assert
        assertEquals(status, response.getStatus());
    }

    @Test
    void testSetStatus_Null() {
        // Arrange
        StatusResponse response = new StatusResponse("ok");

        // Act
        response.setStatus(null);

        // Assert
        assertNull(response.getStatus());
    }

    @Test
    void testSetStatus_EmptyString() {
        // Arrange
        StatusResponse response = new StatusResponse();
        String emptyStatus = "";

        // Act
        response.setStatus(emptyStatus);

        // Assert
        assertEquals(emptyStatus, response.getStatus());
    }
}
