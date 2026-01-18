package com.example.transactions.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SumResponse.
 */
class SumResponseTest {

    @Test
    void testDefaultConstructor() {
        // Act
        SumResponse response = new SumResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getSum());
    }

    @Test
    void testConstructorWithSum() {
        // Arrange
        BigDecimal sum = new BigDecimal("100.50");

        // Act
        SumResponse response = new SumResponse(sum);

        // Assert
        assertNotNull(response);
        assertEquals(sum, response.getSum());
    }

    @Test
    void testGetSum() {
        // Arrange
        BigDecimal sum = new BigDecimal("20000.0");
        SumResponse response = new SumResponse(sum);

        // Act
        BigDecimal result = response.getSum();

        // Assert
        assertEquals(sum, result);
        assertEquals(0, sum.compareTo(result));
    }

    @Test
    void testSetSum() {
        // Arrange
        SumResponse response = new SumResponse();
        BigDecimal sum = new BigDecimal("15000.0");

        // Act
        response.setSum(sum);

        // Assert
        assertEquals(sum, response.getSum());
        assertEquals(0, sum.compareTo(response.getSum()));
    }

    @Test
    void testSetSum_Null() {
        // Arrange
        SumResponse response = new SumResponse(new BigDecimal("100.0"));

        // Act
        response.setSum(null);

        // Assert
        assertNull(response.getSum());
    }

    @Test
    void testSetSum_Zero() {
        // Arrange
        SumResponse response = new SumResponse();
        BigDecimal zero = BigDecimal.ZERO;

        // Act
        response.setSum(zero);

        // Assert
        assertEquals(zero, response.getSum());
        assertTrue(response.getSum().compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    void testSetSum_LargeValue() {
        // Arrange
        SumResponse response = new SumResponse();
        BigDecimal largeValue = new BigDecimal("999999999.99");

        // Act
        response.setSum(largeValue);

        // Assert
        assertEquals(largeValue, response.getSum());
    }

    @Test
    void testSetSum_DecimalPrecision() {
        // Arrange
        SumResponse response = new SumResponse();
        BigDecimal preciseValue = new BigDecimal("123.456789");

        // Act
        response.setSum(preciseValue);

        // Assert
        assertEquals(preciseValue, response.getSum());
        assertEquals(preciseValue.scale(), response.getSum().scale());
    }
}
