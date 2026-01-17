package com.example.transactions.service;

import com.example.transactions.dto.TransactionRequest;
import com.example.transactions.dto.TransactionResponse;
import com.example.transactions.exception.InvalidParentException;
import com.example.transactions.exception.TransactionNotFoundException;
import com.example.transactions.model.Transaction;
import com.example.transactions.repository.TransactionRepository;
import com.example.transactions.repository.TransactionRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TransactionService.
 * Cubre:
 * - Creación de transacciones
 * - Suma sin hijos
 * - Suma con múltiples niveles
 * - Error cuando el parent no existe
 */
class TransactionServiceTest {

    private TransactionRepository repository;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        // Crear una nueva instancia del repositorio para cada test
        repository = new TransactionRepositoryImpl();
        service = new TransactionService(repository);
    }

    @Test
    void testCreateTransaction_Success() {
        // Arrange
        Long id = 1L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.0"),
                "cars",
                null
        );

        // Act
        TransactionResponse response = service.createOrUpdateTransaction(id, request);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(new BigDecimal("100.0"), response.getAmount());
        assertEquals("cars", response.getType());
        assertNull(response.getParentId());
    }

    @Test
    void testCreateTransaction_WithParent_Success() {
        // Arrange
        Long parentId = 1L;
        TransactionRequest parentRequest = new TransactionRequest(
                new BigDecimal("100.0"),
                "cars",
                null
        );
        service.createOrUpdateTransaction(parentId, parentRequest);

        Long id = 2L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("50.0"),
                "shopping",
                parentId
        );

        // Act
        TransactionResponse response = service.createOrUpdateTransaction(id, request);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(new BigDecimal("50.0"), response.getAmount());
        assertEquals("shopping", response.getType());
        assertEquals(parentId, response.getParentId());
    }

    @Test
    void testCreateTransaction_WhenParentDoesNotExist_ThrowsException() {
        // Arrange
        Long id = 2L;
        Long parentId = 999L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("50.0"),
                "shopping",
                parentId
        );

        // Act & Assert
        InvalidParentException exception = assertThrows(
                InvalidParentException.class,
                () -> service.createOrUpdateTransaction(id, request)
        );

        assertTrue(exception.getMessage().contains("no existe"));
    }

    @Test
    void testGetTransactionById_WhenExists_ReturnsTransaction() {
        // Arrange
        Long id = 1L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.0"),
                "cars",
                null
        );
        service.createOrUpdateTransaction(id, request);

        // Act
        TransactionResponse response = service.getTransactionById(id);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(new BigDecimal("100.0"), response.getAmount());
        assertEquals("cars", response.getType());
    }

    @Test
    void testGetTransactionById_WhenNotExists_ThrowsException() {
        // Arrange
        Long id = 999L;

        // Act & Assert
        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> service.getTransactionById(id)
        );

        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    @Test
    void testCalculateSum_WithoutChildren_ReturnsOwnAmount() {
        // Arrange
        Long id = 10L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("5000.0"),
                "cars",
                null
        );
        service.createOrUpdateTransaction(id, request);

        // Act
        BigDecimal sum = service.calculateSum(id);

        // Assert
        assertEquals(new BigDecimal("5000.0"), sum);
    }

    @Test
    void testCalculateSum_WithDirectChildren_ReturnsCorrectSum() {
        // Arrange
        Long parentId = 10L;
        service.createOrUpdateTransaction(parentId, new TransactionRequest(
                new BigDecimal("5000.0"),
                "cars",
                null
        ));

        Long childId1 = 11L;
        service.createOrUpdateTransaction(childId1, new TransactionRequest(
                new BigDecimal("10000.0"),
                "shopping",
                parentId
        ));

        Long childId2 = 12L;
        service.createOrUpdateTransaction(childId2, new TransactionRequest(
                new BigDecimal("5000.0"),
                "shopping",
                parentId
        ));

        // Act
        BigDecimal sum = service.calculateSum(parentId);

        // Assert
        // 5000 (parent) + 10000 (child1) + 5000 (child2) = 20000
        assertEquals(new BigDecimal("20000.0"), sum);
    }

    @Test
    void testCalculateSum_WithMultipleLevels_ReturnsCorrectSum() {
        // Arrange
        // Transacción 10: 5000
        //   └─ Transacción 11: 10000 (parent: 10)
        //      └─ Transacción 12: 5000 (parent: 11)
        
        Long id10 = 10L;
        service.createOrUpdateTransaction(id10, new TransactionRequest(
                new BigDecimal("5000.0"),
                "cars",
                null
        ));

        Long id11 = 11L;
        service.createOrUpdateTransaction(id11, new TransactionRequest(
                new BigDecimal("10000.0"),
                "shopping",
                id10
        ));

        Long id12 = 12L;
        service.createOrUpdateTransaction(id12, new TransactionRequest(
                new BigDecimal("5000.0"),
                "shopping",
                id11
        ));

        // Act
        BigDecimal sum = service.calculateSum(id10);

        // Assert
        // 5000 (t10) + 10000 (t11) + 5000 (t12) = 20000
        assertEquals(new BigDecimal("20000.0"), sum);

        // Verificar que se calculó la suma de t11 correctamente
        BigDecimal sum11 = service.calculateSum(id11);
        // 10000 (t11) + 5000 (t12) = 15000
        assertEquals(new BigDecimal("15000.0"), sum11);
    }

    @Test
    void testCalculateSum_WhenNotExists_ThrowsException() {
        // Arrange
        Long id = 999L;

        // Act & Assert
        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> service.calculateSum(id)
        );

        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    @Test
    void testCreateTransaction_WithSelfAsParent_ThrowsException() {
        // Arrange
        Long id = 1L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.0"),
                "cars",
                id  // intentando ser su propio padre
        );

        // Act & Assert
        InvalidParentException exception = assertThrows(
                InvalidParentException.class,
                () -> service.createOrUpdateTransaction(id, request)
        );

        assertTrue(exception.getMessage().contains("no puede ser su propio padre"));
    }

    @Test
    void testGetTransactionIdsByType_ReturnsCorrectIds() {
        // Arrange
        service.createOrUpdateTransaction(10L, new TransactionRequest(
                new BigDecimal("5000.0"),
                "cars",
                null
        ));
        service.createOrUpdateTransaction(20L, new TransactionRequest(
                new BigDecimal("3000.0"),
                "cars",
                null
        ));
        service.createOrUpdateTransaction(30L, new TransactionRequest(
                new BigDecimal("2000.0"),
                "shopping",
                null
        ));

        // Act
        var result = service.getTransactionIdsByType("cars");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(10L));
        assertTrue(result.contains(20L));
        assertFalse(result.contains(30L));
    }
}
