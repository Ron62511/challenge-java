package com.example.transactions.integration;

import com.example.transactions.controller.TransactionController;
import com.example.transactions.dto.TransactionRequest;
import com.example.transactions.exception.GlobalExceptionHandler;
import com.example.transactions.repository.TransactionRepository;
import com.example.transactions.repository.TransactionRepositoryImpl;
import com.example.transactions.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración end-to-end para la API de transacciones.
 * 
 * Prueba el flujo completo desde las peticiones HTTP hasta la persistencia en memoria,
 * incluyendo todas las capas: Controller → Service → Repository.
 * 
 * Construye los componentes manualmente para evitar problemas con Mockito en macOS.
 */
class TransactionIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TransactionRepository repository;
    private TransactionService service;
    private TransactionController controller;

    @BeforeEach
    void setUp() {
        // Construir toda la cadena de dependencias manualmente
        repository = new TransactionRepositoryImpl();
        service = new TransactionService(repository);
        controller = new TransactionController(service);
        
        // Construir MockMvc sin usar Spring's @AutoConfigureMockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
    }

    // ========== Tests End-to-End para PUT /transactions/{id} ==========

    @Test
    void testCreateTransaction_EndToEnd_Success() throws Exception {
        // Arrange
        Long transactionId = 10L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("5000.0"),
                "cars",
                null
        );

        // Act & Assert - Crear transacción
        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        // Verificar que la transacción se guardó correctamente
        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.amount").value(5000.0))
                .andExpect(jsonPath("$.type").value("cars"))
                .andExpect(jsonPath("$.parent_id").isEmpty());
    }

    @Test
    void testCreateTransaction_WithParentId_EndToEnd_Success() throws Exception {
        // Arrange - Crear transacción padre
        Long parentId = 1L;
        TransactionRequest parentRequest = new TransactionRequest(
                new BigDecimal("10000.0"),
                "cars",
                null
        );
        createTransaction(parentId, parentRequest);

        // Crear transacción hija
        Long childId = 2L;
        TransactionRequest childRequest = new TransactionRequest(
                new BigDecimal("5000.0"),
                "shopping",
                parentId
        );

        // Act & Assert - Crear transacción hija
        mockMvc.perform(put("/transactions/{id}", childId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(childRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        // Verificar la transacción hija
        mockMvc.perform(get("/transactions/{id}", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(childId))
                .andExpect(jsonPath("$.parent_id").value(parentId))
                .andExpect(jsonPath("$.type").value("shopping"));
    }

    @Test
    void testUpdateTransaction_EndToEnd_Success() throws Exception {
        // Arrange - Crear transacción
        Long transactionId = 20L;
        TransactionRequest originalRequest = new TransactionRequest(
                new BigDecimal("1000.0"),
                "cars",
                null
        );
        createTransaction(transactionId, originalRequest);

        // Actualizar transacción
        TransactionRequest updateRequest = new TransactionRequest(
                new BigDecimal("2000.0"),
                "shopping",
                null
        );

        // Act & Assert - Actualizar transacción
        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        // Verificar que se actualizó
        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(2000.0))
                .andExpect(jsonPath("$.type").value("shopping"));
    }

    @Test
    void testCreateTransaction_InvalidParentId_EndToEnd_BadRequest() throws Exception {
        // Arrange
        Long transactionId = 30L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("1000.0"),
                "cars",
                999L // ParentId que no existe
        );

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateTransaction_Validation_AmountNull_EndToEnd_BadRequest() throws Exception {
        // Arrange
        Long transactionId = 40L;
        TransactionRequest request = new TransactionRequest(
                null, // amount null - viola @NotNull
                "cars",
                null
        );

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_Validation_AmountNegative_EndToEnd_BadRequest() throws Exception {
        // Arrange
        Long transactionId = 50L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("-10.0"), // amount negativo - viola @Positive
                "cars",
                null
        );

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== Tests End-to-End para GET /transactions/{id} ==========

    @Test
    void testGetTransaction_EndToEnd_Success() throws Exception {
        // Arrange
        Long transactionId = 60L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("3000.0"),
                "cars",
                null
        );
        createTransaction(transactionId, request);

        // Act & Assert
        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.amount").value(3000.0))
                .andExpect(jsonPath("$.type").value("cars"))
                .andExpect(jsonPath("$.parent_id").isEmpty());
    }

    @Test
    void testGetTransaction_NotFound_EndToEnd_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/transactions/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ========== Tests End-to-End para GET /transactions/types/{type} ==========

    @Test
    void testGetTransactionIdsByType_EndToEnd_Success() throws Exception {
        // Arrange - Crear múltiples transacciones del mismo tipo
        createTransaction(70L, new TransactionRequest(new BigDecimal("1000.0"), "cars", null));
        createTransaction(71L, new TransactionRequest(new BigDecimal("2000.0"), "shopping", null));
        createTransaction(72L, new TransactionRequest(new BigDecimal("3000.0"), "cars", null));
        createTransaction(73L, new TransactionRequest(new BigDecimal("4000.0"), "cars", null));
        createTransaction(74L, new TransactionRequest(new BigDecimal("5000.0"), "shopping", null));

        // Act & Assert
        mockMvc.perform(get("/transactions/types/{type}", "cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists())
                .andExpect(jsonPath("$[2]").exists());
    }

    @Test
    void testGetTransactionIdsByType_NonExistingType_EndToEnd_EmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/transactions/types/{type}", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ========== Tests End-to-End para GET /transactions/sum/{id} ==========

    @Test
    void testGetTransactionSum_WithoutChildren_EndToEnd_Success() throws Exception {
        // Arrange
        Long transactionId = 80L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("10000.0"),
                "cars",
                null
        );
        createTransaction(transactionId, request);

        // Act & Assert
        mockMvc.perform(get("/transactions/sum/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(10000.0));
    }

    @Test
    void testGetTransactionSum_WithChildren_EndToEnd_Success() throws Exception {
        // Arrange - Crear jerarquía: parent -> child1, child2 -> child3
        Long parentId = 90L;
        Long child1Id = 91L;
        Long child2Id = 92L;
        Long child3Id = 93L;

        // Parent: 10000
        createTransaction(parentId, new TransactionRequest(new BigDecimal("10000.0"), "cars", null));
        
        // Child1: 5000 (hijo de parent)
        createTransaction(child1Id, new TransactionRequest(new BigDecimal("5000.0"), "shopping", parentId));
        
        // Child2: 3000 (hijo de parent)
        createTransaction(child2Id, new TransactionRequest(new BigDecimal("3000.0"), "food", parentId));
        
        // Child3: 2000 (hijo de child2)
        createTransaction(child3Id, new TransactionRequest(new BigDecimal("2000.0"), "clothes", child2Id));

        // Act & Assert - Suma del parent debe incluir todos los descendientes
        // Parent suma: 10000 + 5000 + 3000 + 2000 = 20000
        mockMvc.perform(get("/transactions/sum/{id}", parentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(20000.0));

        // Act & Assert - Suma de child2 debe incluir child3
        // Child2 suma: 3000 + 2000 = 5000
        mockMvc.perform(get("/transactions/sum/{id}", child2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(5000.0));

        // Act & Assert - Suma de child1 debe ser solo su monto
        // Child1 suma: 5000
        mockMvc.perform(get("/transactions/sum/{id}", child1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(5000.0));
    }

    @Test
    void testGetTransactionSum_NotFound_EndToEnd_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/transactions/sum/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // ========== Tests End-to-End para Ciclos ==========

    @Test
    void testCreateTransaction_CycleDetection_EndToEnd_BadRequest() throws Exception {
        // Arrange - Crear jerarquía: 1 -> 2 -> 3
        Long id1 = 100L;
        Long id2 = 101L;
        Long id3 = 102L;

        createTransaction(id1, new TransactionRequest(new BigDecimal("1000.0"), "cars", null));
        createTransaction(id2, new TransactionRequest(new BigDecimal("2000.0"), "shopping", id1));
        createTransaction(id3, new TransactionRequest(new BigDecimal("3000.0"), "food", id2));

        // Act - Intentar crear un ciclo: hacer que id1 sea hijo de id3
        TransactionRequest cycleRequest = new TransactionRequest(
                new BigDecimal("1000.0"),
                "cars",
                id3 // Esto crearía un ciclo: 1 -> 2 -> 3 -> 1
        );

        // Assert
        mockMvc.perform(put("/transactions/{id}", id1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cycleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ========== Tests End-to-End para Flujo Completo ==========

    @Test
    void testCompleteFlow_EndToEnd_Success() throws Exception {
        // 1. Crear transacciones padre
        Long parent1 = 200L;
        Long parent2 = 201L;
        
        createTransaction(parent1, new TransactionRequest(new BigDecimal("10000.0"), "cars", null));
        createTransaction(parent2, new TransactionRequest(new BigDecimal("20000.0"), "shopping", null));

        // 2. Crear transacciones hijas
        Long child1 = 202L;
        Long child2 = 203L;
        
        createTransaction(child1, new TransactionRequest(new BigDecimal("5000.0"), "shopping", parent1));
        createTransaction(child2, new TransactionRequest(new BigDecimal("3000.0"), "food", parent1));

        // 3. Verificar que se pueden obtener por ID
        mockMvc.perform(get("/transactions/{id}", parent1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(10000.0));
        
        mockMvc.perform(get("/transactions/{id}", child1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parent_id").value(parent1));

        // 4. Verificar búsqueda por tipo
        mockMvc.perform(get("/transactions/types/{type}", "cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/transactions/types/{type}", "shopping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // 5. Verificar sumas
        // Parent1: 10000 + 5000 + 3000 = 18000
        mockMvc.perform(get("/transactions/sum/{id}", parent1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(18000.0));

        // Parent2: 20000 (sin hijos)
        mockMvc.perform(get("/transactions/sum/{id}", parent2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(20000.0));

        // 6. Actualizar una transacción
        TransactionRequest updateRequest = new TransactionRequest(
                new BigDecimal("15000.0"),
                "cars",
                null
        );
        createTransaction(parent1, updateRequest);

        // 7. Verificar que se actualizó
        mockMvc.perform(get("/transactions/{id}", parent1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(15000.0))
                .andExpect(jsonPath("$.type").value("cars"));

        // 8. Verificar que la suma cambió
        // Parent1 actualizado: 15000 + 5000 + 3000 = 23000
        mockMvc.perform(get("/transactions/sum/{id}", parent1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(23000.0));
    }

    // ========== Métodos auxiliares ==========

    private void createTransaction(Long id, TransactionRequest request) throws Exception {
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
