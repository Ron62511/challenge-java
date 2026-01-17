package com.example.transactions.service;

import com.example.transactions.dto.TransactionRequest;
import com.example.transactions.dto.TransactionResponse;
import com.example.transactions.exception.InvalidParentException;
import com.example.transactions.exception.TransactionNotFoundException;
import com.example.transactions.model.Transaction;
import com.example.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TransactionService.
 * Cubre:
 * - Creación de transacciones
 * - Suma sin hijos
 * - Suma con múltiples niveles
 * - Error cuando el parent no existe
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

    @BeforeEach
    void setUp() {
        // Resetear el mock antes de cada test
        reset(repository);
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

        when(repository.findById(id)).thenReturn(null);
        when(repository.existsById(id)).thenReturn(false);
        doNothing().when(repository).save(any(Transaction.class));

        // Act
        TransactionResponse response = service.createOrUpdateTransaction(id, request);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(new BigDecimal("100.0"), response.getAmount());
        assertEquals("cars", response.getType());
        assertNull(response.getParentId());

        verify(repository).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_WithParent_Success() {
        // Arrange
        Long id = 2L;
        Long parentId = 1L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("50.0"),
                "shopping",
                parentId
        );

        Transaction parent = new Transaction(parentId, new BigDecimal("100.0"), "cars", null);

        when(repository.findById(id)).thenReturn(null);
        when(repository.findById(parentId)).thenReturn(parent);
        when(repository.findByParentId(anyLong())).thenReturn(new ArrayList<>());
        doNothing().when(repository).save(any(Transaction.class));

        // Act
        TransactionResponse response = service.createOrUpdateTransaction(id, request);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(new BigDecimal("50.0"), response.getAmount());
        assertEquals("shopping", response.getType());
        assertEquals(parentId, response.getParentId());

        verify(repository).save(any(Transaction.class));
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

        when(repository.findById(id)).thenReturn(null);
        when(repository.findById(parentId)).thenReturn(null);

        // Act & Assert
        InvalidParentException exception = assertThrows(
                InvalidParentException.class,
                () -> service.createOrUpdateTransaction(id, request)
        );

        assertTrue(exception.getMessage().contains("no existe"));
        verify(repository, never()).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionById_WhenExists_ReturnsTransaction() {
        // Arrange
        Long id = 1L;
        Transaction transaction = new Transaction(
                id,
                new BigDecimal("100.0"),
                "cars",
                null
        );

        when(repository.findById(id)).thenReturn(transaction);

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
        when(repository.findById(id)).thenReturn(null);

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
        Transaction transaction = new Transaction(
                id,
                new BigDecimal("5000.0"),
                "cars",
                null
        );

        when(repository.findById(id)).thenReturn(transaction);
        when(repository.findByParentId(id)).thenReturn(new ArrayList<>());

        // Act
        BigDecimal sum = service.calculateSum(id);

        // Assert
        assertEquals(new BigDecimal("5000.0"), sum);
        verify(repository).findById(id);
        verify(repository).findByParentId(id);
    }

    @Test
    void testCalculateSum_WithDirectChildren_ReturnsCorrectSum() {
        // Arrange
        Long parentId = 10L;
        Transaction parent = new Transaction(
                parentId,
                new BigDecimal("5000.0"),
                "cars",
                null
        );

        Long childId1 = 11L;
        Transaction child1 = new Transaction(
                childId1,
                new BigDecimal("10000.0"),
                "shopping",
                parentId
        );

        Long childId2 = 12L;
        Transaction child2 = new Transaction(
                childId2,
                new BigDecimal("5000.0"),
                "shopping",
                parentId
        );

        List<Transaction> children = List.of(child1, child2);

        when(repository.findById(parentId)).thenReturn(parent);
        when(repository.findByParentId(parentId)).thenReturn(children);
        when(repository.findById(childId1)).thenReturn(child1);
        when(repository.findById(childId2)).thenReturn(child2);
        when(repository.findByParentId(childId1)).thenReturn(new ArrayList<>());
        when(repository.findByParentId(childId2)).thenReturn(new ArrayList<>());

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
        Transaction t10 = new Transaction(id10, new BigDecimal("5000.0"), "cars", null);

        Long id11 = 11L;
        Transaction t11 = new Transaction(id11, new BigDecimal("10000.0"), "shopping", id10);

        Long id12 = 12L;
        Transaction t12 = new Transaction(id12, new BigDecimal("5000.0"), "shopping", id11);

        when(repository.findById(id10)).thenReturn(t10);
        when(repository.findByParentId(id10)).thenReturn(List.of(t11));
        
        when(repository.findById(id11)).thenReturn(t11);
        when(repository.findByParentId(id11)).thenReturn(List.of(t12));
        
        when(repository.findById(id12)).thenReturn(t12);
        when(repository.findByParentId(id12)).thenReturn(new ArrayList<>());

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
        when(repository.findById(id)).thenReturn(null);

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

        when(repository.findById(id)).thenReturn(null);

        // Act & Assert
        InvalidParentException exception = assertThrows(
                InvalidParentException.class,
                () -> service.createOrUpdateTransaction(id, request)
        );

        assertTrue(exception.getMessage().contains("no puede ser su propio padre"));
        verify(repository, never()).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionIdsByType_ReturnsCorrectIds() {
        // Arrange
        String type = "cars";
        List<Long> expectedIds = List.of(10L, 20L, 30L);

        when(repository.findIdsByType(type)).thenReturn(expectedIds);

        // Act
        List<Long> result = service.getTransactionIdsByType(type);

        // Assert
        assertEquals(expectedIds, result);
        verify(repository).findIdsByType(type);
    }
}
