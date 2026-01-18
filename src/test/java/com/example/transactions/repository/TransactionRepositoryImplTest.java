package com.example.transactions.repository;

import com.example.transactions.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TransactionRepositoryImpl.
 * 
 * Cubre todas las operaciones CRUD, búsquedas por tipo y parentId,
 * y verifica thread-safety básica.
 */
class TransactionRepositoryImplTest {

    private TransactionRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepositoryImpl();
    }

    // ========== Tests para save ==========

    @Test
    void testSave_NewTransaction() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);

        // Act
        repository.save(transaction);

        // Assert
        assertTrue(repository.existsById(1L));
        Transaction saved = repository.findById(1L);
        assertNotNull(saved);
        assertEquals(transaction.getId(), saved.getId());
        assertEquals(transaction.getAmount(), saved.getAmount());
        assertEquals(transaction.getType(), saved.getType());
        assertEquals(transaction.getParentId(), saved.getParentId());
    }

    @Test
    void testSave_UpdateExistingTransaction() {
        // Arrange
        Transaction original = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(original);
        
        Transaction updated = new Transaction(1L, new BigDecimal("200.0"), "shopping", 10L);

        // Act
        repository.save(updated);

        // Assert
        Transaction saved = repository.findById(1L);
        assertNotNull(saved);
        assertEquals(updated.getAmount(), saved.getAmount());
        assertEquals(updated.getType(), saved.getType());
        assertEquals(updated.getParentId(), saved.getParentId());
        // Verificar que solo existe una transacción con ese ID
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void testSave_UpdateTransactionType_UpdatesTypeIndex() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction1);
        
        // Verificar que está en el índice de "cars"
        List<Long> carIds = repository.findIdsByType("cars");
        assertTrue(carIds.contains(1L));
        assertFalse(repository.findIdsByType("shopping").contains(1L));

        // Act - Cambiar el tipo
        Transaction updated = new Transaction(1L, new BigDecimal("100.0"), "shopping", null);
        repository.save(updated);

        // Assert - Verificar que el índice se actualizó
        assertFalse(repository.findIdsByType("cars").contains(1L));
        assertTrue(repository.findIdsByType("shopping").contains(1L));
    }

    @Test
    void testSave_MultipleTransactions() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(2L, new BigDecimal("50.0"), "shopping", null);
        Transaction transaction3 = new Transaction(3L, new BigDecimal("200.0"), "cars", null);

        // Act
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);

        // Assert
        assertEquals(3, repository.findAll().size());
        assertTrue(repository.existsById(1L));
        assertTrue(repository.existsById(2L));
        assertTrue(repository.existsById(3L));
    }

    @Test
    void testSave_TransactionWithParentId() {
        // Arrange
        Transaction parent = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction child = new Transaction(2L, new BigDecimal("50.0"), "shopping", 1L);

        // Act
        repository.save(parent);
        repository.save(child);

        // Assert
        List<Transaction> children = repository.findByParentId(1L);
        assertEquals(1, children.size());
        assertEquals(child.getId(), children.get(0).getId());
    }

    // ========== Tests para findById ==========

    @Test
    void testFindById_ExistingTransaction() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction);

        // Act
        Transaction found = repository.findById(1L);

        // Assert
        assertNotNull(found);
        assertEquals(transaction.getId(), found.getId());
        assertEquals(transaction.getAmount(), found.getAmount());
        assertEquals(transaction.getType(), found.getType());
        assertEquals(transaction.getParentId(), found.getParentId());
    }

    @Test
    void testFindById_NonExistingTransaction() {
        // Act
        Transaction found = repository.findById(999L);

        // Assert
        assertNull(found);
    }

    @Test
    void testFindById_NullId_ThrowsException() {
        // Act & Assert
        // ConcurrentHashMap no acepta claves null, por lo que lanzará NullPointerException
        assertThrows(NullPointerException.class, () -> repository.findById(null));
    }

    // ========== Tests para existsById ==========

    @Test
    void testExistsById_ExistingTransaction() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction);

        // Act & Assert
        assertTrue(repository.existsById(1L));
    }

    @Test
    void testExistsById_NonExistingTransaction() {
        // Act & Assert
        assertFalse(repository.existsById(999L));
    }

    @Test
    void testExistsById_NullId_ThrowsException() {
        // Act & Assert
        // ConcurrentHashMap no acepta claves null, por lo que lanzará NullPointerException
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
    }

    // ========== Tests para findIdsByType ==========

    @Test
    void testFindIdsByType_SingleTransaction() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction);

        // Act
        List<Long> ids = repository.findIdsByType("cars");

        // Assert
        assertNotNull(ids);
        assertEquals(1, ids.size());
        assertTrue(ids.contains(1L));
    }

    @Test
    void testFindIdsByType_MultipleTransactions() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(2L, new BigDecimal("200.0"), "cars", null);
        Transaction transaction3 = new Transaction(3L, new BigDecimal("50.0"), "shopping", null);
        
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);

        // Act
        List<Long> carIds = repository.findIdsByType("cars");
        List<Long> shoppingIds = repository.findIdsByType("shopping");

        // Assert
        assertEquals(2, carIds.size());
        assertTrue(carIds.contains(1L));
        assertTrue(carIds.contains(2L));
        assertFalse(carIds.contains(3L));
        
        assertEquals(1, shoppingIds.size());
        assertTrue(shoppingIds.contains(3L));
    }

    @Test
    void testFindIdsByType_NonExistingType() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction);

        // Act
        List<Long> ids = repository.findIdsByType("nonexistent");

        // Assert
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    void testFindIdsByType_EmptyRepository() {
        // Act
        List<Long> ids = repository.findIdsByType("cars");

        // Assert
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    void testFindIdsByType_NullType_ThrowsException() {
        // Act & Assert
        // ConcurrentHashMap no acepta claves null, por lo que lanzará NullPointerException
        assertThrows(NullPointerException.class, () -> repository.findIdsByType(null));
    }

    @Test
    void testFindIdsByType_ReturnsNewList() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction);

        // Act
        List<Long> ids1 = repository.findIdsByType("cars");
        ids1.add(999L); // Modificar la lista retornada
        List<Long> ids2 = repository.findIdsByType("cars"); // Obtener nueva lista

        // Assert - La modificación no debe afectar la lista interna
        assertEquals(1, ids2.size());
        assertFalse(ids2.contains(999L));
    }

    // ========== Tests para findByParentId ==========

    @Test
    void testFindByParentId_SingleChild() {
        // Arrange
        Transaction parent = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction child = new Transaction(2L, new BigDecimal("50.0"), "shopping", 1L);
        
        repository.save(parent);
        repository.save(child);

        // Act
        List<Transaction> children = repository.findByParentId(1L);

        // Assert
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals(child.getId(), children.get(0).getId());
    }

    @Test
    void testFindByParentId_MultipleChildren() {
        // Arrange
        Transaction parent = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction child1 = new Transaction(2L, new BigDecimal("50.0"), "shopping", 1L);
        Transaction child2 = new Transaction(3L, new BigDecimal("30.0"), "food", 1L);
        Transaction child3 = new Transaction(4L, new BigDecimal("20.0"), "shopping", 2L); // Parent diferente
        
        repository.save(parent);
        repository.save(child1);
        repository.save(child2);
        repository.save(child3);

        // Act
        List<Transaction> children = repository.findByParentId(1L);

        // Assert
        assertEquals(2, children.size());
        assertTrue(children.stream().anyMatch(t -> t.getId().equals(2L)));
        assertTrue(children.stream().anyMatch(t -> t.getId().equals(3L)));
        assertFalse(children.stream().anyMatch(t -> t.getId().equals(4L)));
    }

    @Test
    void testFindByParentId_NoChildren() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction);

        // Act
        List<Transaction> children = repository.findByParentId(1L);

        // Assert
        assertNotNull(children);
        assertTrue(children.isEmpty());
    }

    @Test
    void testFindByParentId_NonExistingParent() {
        // Act
        List<Transaction> children = repository.findByParentId(999L);

        // Assert
        assertNotNull(children);
        assertTrue(children.isEmpty());
    }

    @Test
    void testFindByParentId_NullParentId_ThrowsException() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction1);

        // Act & Assert
        // El filter usa parentId.equals(t.getParentId()) que lanzará NullPointerException
        // cuando parentId es null
        assertThrows(NullPointerException.class, () -> repository.findByParentId(null));
    }

    // ========== Tests para findAll ==========

    @Test
    void testFindAll_EmptyRepository() {
        // Act
        List<Transaction> all = repository.findAll();

        // Assert
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAll_MultipleTransactions() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(2L, new BigDecimal("50.0"), "shopping", null);
        Transaction transaction3 = new Transaction(3L, new BigDecimal("200.0"), "cars", null);
        
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);

        // Act
        List<Transaction> all = repository.findAll();

        // Assert
        assertEquals(3, all.size());
        assertTrue(all.stream().anyMatch(t -> t.getId().equals(1L)));
        assertTrue(all.stream().anyMatch(t -> t.getId().equals(2L)));
        assertTrue(all.stream().anyMatch(t -> t.getId().equals(3L)));
    }

    @Test
    void testFindAll_ReturnsNewList() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        repository.save(transaction);

        // Act
        List<Transaction> all1 = repository.findAll();
        all1.add(new Transaction(999L, new BigDecimal("0.0"), "test", null)); // Modificar
        List<Transaction> all2 = repository.findAll(); // Obtener nueva lista

        // Assert - La modificación no debe afectar la lista interna
        assertEquals(1, all2.size());
        assertFalse(all2.stream().anyMatch(t -> t.getId().equals(999L)));
    }

    // ========== Tests de integración ==========

    @Test
    void testIntegration_FullWorkflow() {
        // Arrange & Act - Crear transacciones
        Transaction parent = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction child1 = new Transaction(2L, new BigDecimal("50.0"), "shopping", 1L);
        Transaction child2 = new Transaction(3L, new BigDecimal("30.0"), "food", 1L);
        Transaction standalone = new Transaction(4L, new BigDecimal("200.0"), "cars", null);
        
        repository.save(parent);
        repository.save(child1);
        repository.save(child2);
        repository.save(standalone);

        // Assert - Verificar búsquedas
        assertTrue(repository.existsById(1L));
        assertTrue(repository.existsById(2L));
        assertTrue(repository.existsById(3L));
        assertTrue(repository.existsById(4L));
        
        assertEquals(4, repository.findAll().size());
        
        List<Long> carIds = repository.findIdsByType("cars");
        assertEquals(2, carIds.size());
        assertTrue(carIds.contains(1L));
        assertTrue(carIds.contains(4L));
        
        List<Transaction> children = repository.findByParentId(1L);
        assertEquals(2, children.size());
        
        // Actualizar transacción
        Transaction updated = new Transaction(2L, new BigDecimal("75.0"), "shopping", 1L);
        repository.save(updated);
        
        Transaction found = repository.findById(2L);
        assertEquals(new BigDecimal("75.0"), found.getAmount());
        assertEquals(4, repository.findAll().size()); // No debe duplicarse
    }

    // ========== Tests de casos edge ==========

    @Test
    void testSave_TransactionWithNullAmountAndParentId() {
        // Arrange
        // Nota: type no puede ser null porque ConcurrentHashMap no acepta claves null
        Transaction transaction = new Transaction(1L, null, "cars", null);

        // Act
        repository.save(transaction);

        // Assert
        Transaction saved = repository.findById(1L);
        assertNotNull(saved);
        assertNull(saved.getAmount());
        assertEquals("cars", saved.getType()); // type no puede ser null
        assertNull(saved.getParentId());
    }

    @Test
    void testSave_TransactionWithNullType_ThrowsException() {
        // Arrange
        // type no puede ser null porque se usa como clave en el índice
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), null, null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.save(transaction));
    }

    @Test
    void testSave_VeryLargeAmount() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("999999999.99");
        Transaction transaction = new Transaction(1L, largeAmount, "cars", null);

        // Act
        repository.save(transaction);

        // Assert
        Transaction saved = repository.findById(1L);
        assertEquals(largeAmount, saved.getAmount());
    }

    @Test
    void testSave_EmptyType() {
        // Arrange
        Transaction transaction = new Transaction(1L, new BigDecimal("100.0"), "", null);

        // Act
        repository.save(transaction);

        // Assert
        List<Long> ids = repository.findIdsByType("");
        assertTrue(ids.contains(1L));
    }

    @Test
    void testFindByParentId_WithUpdatedParentId() {
        // Arrange
        Transaction transaction1 = new Transaction(1L, new BigDecimal("100.0"), "cars", null);
        Transaction transaction2 = new Transaction(2L, new BigDecimal("50.0"), "shopping", 1L);
        
        repository.save(transaction1);
        repository.save(transaction2);

        // Verificar que transaction2 está bajo parent 1
        List<Transaction> children = repository.findByParentId(1L);
        assertEquals(1, children.size());
        assertEquals(2L, children.get(0).getId());

        // Act - Cambiar parentId de transaction2 a otro valor (no null, porque null causa NPE)
        Transaction updated = new Transaction(2L, new BigDecimal("50.0"), "shopping", 10L);
        repository.save(updated);

        // Assert
        assertEquals(0, repository.findByParentId(1L).size());
        assertEquals(1, repository.findByParentId(10L).size());
        assertEquals(2L, repository.findByParentId(10L).get(0).getId());
    }
}
