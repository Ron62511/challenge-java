package com.example.transactions.controller;

import com.example.transactions.dto.StatusResponse;
import com.example.transactions.dto.SumResponse;
import com.example.transactions.dto.TransactionRequest;
import com.example.transactions.dto.TransactionResponse;
import com.example.transactions.exception.InvalidParentException;
import com.example.transactions.exception.TransactionNotFoundException;
import com.example.transactions.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para TransactionController.
 * 
 * Usa MockMvc standalone sin @WebMvcTest para evitar problemas con Mockito en macOS.
 * 
 * Tests cubren:
 * - Validaciones de entrada (@Valid)
 * - Códigos de respuesta HTTP
 * - Manejo de excepciones
 * - Transformación HTTP ⇄ DTO
 */
class TransactionControllerTest {

    private MockMvc mockMvc;
    private TransactionService transactionService;
    private ObjectMapper objectMapper;

    private TransactionRequest validRequest;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        // Crear un mock del service manualmente
        transactionService = mock(TransactionService.class);
        
        // Crear el controller con el mock
        TransactionController controller = new TransactionController(transactionService);
        
        // Configurar MockMvc standalone sin Spring context
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.example.transactions.exception.GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        validRequest = new TransactionRequest(
                new BigDecimal("100.0"),
                "cars",
                null
        );

        transactionResponse = new TransactionResponse(
                1L,
                new BigDecimal("100.0"),
                "cars",
                null
        );
    }

    // ========== Tests para PUT /transactions/{id} ==========

    @Test
    void testCreateOrUpdateTransaction_Success() throws Exception {
        // Arrange
        Long id = 1L;
        when(transactionService.createOrUpdateTransaction(eq(id), any(TransactionRequest.class)))
                .thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        verify(transactionService).createOrUpdateTransaction(eq(id), any(TransactionRequest.class));
    }

    @Test
    void testCreateOrUpdateTransaction_WithParentId_Success() throws Exception {
        // Arrange
        Long id = 2L;
        Long parentId = 1L;
        TransactionRequest requestWithParent = new TransactionRequest(
                new BigDecimal("50.0"),
                "shopping",
                parentId
        );
        TransactionResponse response = new TransactionResponse(id, new BigDecimal("50.0"), "shopping", parentId);

        when(transactionService.createOrUpdateTransaction(eq(id), any(TransactionRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithParent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        verify(transactionService).createOrUpdateTransaction(eq(id), any(TransactionRequest.class));
    }

    @Test
    void testCreateOrUpdateTransaction_Validation_AmountNull_ReturnsBadRequest() throws Exception {
        // Arrange
        Long id = 1L;
        TransactionRequest invalidRequest = new TransactionRequest(
                null,  // amount es null - viola @NotNull
                "cars",
                null
        );

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).createOrUpdateTransaction(any(), any());
    }

    @Test
    void testCreateOrUpdateTransaction_Validation_AmountNegative_ReturnsBadRequest() throws Exception {
        // Arrange
        Long id = 1L;
        TransactionRequest invalidRequest = new TransactionRequest(
                new BigDecimal("-10.0"),  // amount negativo - viola @Positive
                "cars",
                null
        );

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).createOrUpdateTransaction(any(), any());
    }

    @Test
    void testCreateOrUpdateTransaction_Validation_AmountZero_ReturnsBadRequest() throws Exception {
        // Arrange
        Long id = 1L;
        TransactionRequest invalidRequest = new TransactionRequest(
                BigDecimal.ZERO,  // amount es cero - viola @Positive
                "cars",
                null
        );

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).createOrUpdateTransaction(any(), any());
    }

    @Test
    void testCreateOrUpdateTransaction_Validation_TypeNull_ReturnsBadRequest() throws Exception {
        // Arrange
        Long id = 1L;
        TransactionRequest invalidRequest = new TransactionRequest(
                new BigDecimal("100.0"),
                null,  // type es null - viola @NotNull
                null
        );

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).createOrUpdateTransaction(any(), any());
    }

    @Test
    void testCreateOrUpdateTransaction_Validation_EmptyRequestBody_ReturnsBadRequest() throws Exception {
        // Arrange
        Long id = 1L;

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))  // Request body vacío
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).createOrUpdateTransaction(any(), any());
    }

    @Test
    void testCreateOrUpdateTransaction_InvalidParentId_ReturnsBadRequest() throws Exception {
        // Arrange
        Long id = 2L;
        Long invalidParentId = 999L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("50.0"),
                "shopping",
                invalidParentId
        );

        when(transactionService.createOrUpdateTransaction(eq(id), any(TransactionRequest.class)))
                .thenThrow(new InvalidParentException("La transacción padre con ID " + invalidParentId + " no existe"));

        // Act & Assert
        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(transactionService).createOrUpdateTransaction(eq(id), any(TransactionRequest.class));
    }

    // ========== Tests para GET /transactions/{id} ==========

    @Test
    void testGetTransaction_Success() throws Exception {
        // Arrange
        Long id = 1L;
        when(transactionService.getTransactionById(id)).thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(get("/transactions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.type").value("cars"))
                .andExpect(jsonPath("$.parent_id").isEmpty());

        verify(transactionService).getTransactionById(id);
    }

    @Test
    void testGetTransaction_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        Long id = 999L;
        when(transactionService.getTransactionById(id))
                .thenThrow(new TransactionNotFoundException("Transacción con ID " + id + " no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/transactions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        verify(transactionService).getTransactionById(id);
    }

    // ========== Tests para GET /transactions/types/{type} ==========

    @Test
    void testGetTransactionIdsByType_Success() throws Exception {
        // Arrange
        String type = "cars";
        List<Long> ids = Arrays.asList(1L, 3L, 7L);
        when(transactionService.getTransactionIdsByType(type)).thenReturn(ids);

        // Act & Assert
        mockMvc.perform(get("/transactions/types/{type}", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(3))
                .andExpect(jsonPath("$[2]").value(7))
                .andExpect(jsonPath("$.length()").value(3));

        verify(transactionService).getTransactionIdsByType(type);
    }

    @Test
    void testGetTransactionIdsByType_EmptyList_Success() throws Exception {
        // Arrange
        String type = "nonexistent";
        when(transactionService.getTransactionIdsByType(type)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/transactions/types/{type}", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(transactionService).getTransactionIdsByType(type);
    }

    // ========== Tests para GET /transactions/sum/{id} ==========

    @Test
    void testGetTransactionSum_Success() throws Exception {
        // Arrange
        Long id = 10L;
        BigDecimal sum = new BigDecimal("20000.0");
        when(transactionService.calculateSum(id)).thenReturn(sum);

        // Act & Assert
        mockMvc.perform(get("/transactions/sum/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(20000.0));

        verify(transactionService).calculateSum(id);
    }

    @Test
    void testGetTransactionSum_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        Long id = 999L;
        when(transactionService.calculateSum(id))
                .thenThrow(new TransactionNotFoundException("Transacción con ID " + id + " no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/transactions/sum/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        verify(transactionService).calculateSum(id);
    }

    @Test
    void testGetTransactionSum_WithHierarchy_Success() throws Exception {
        // Arrange
        Long id = 10L;
        BigDecimal sum = new BigDecimal("15000.0");
        when(transactionService.calculateSum(id)).thenReturn(sum);

        // Act & Assert
        mockMvc.perform(get("/transactions/sum/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(15000.0));

        verify(transactionService).calculateSum(id);
    }
}
