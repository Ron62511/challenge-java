package com.example.transactions.controller;

import com.example.transactions.dto.StatusResponse;
import com.example.transactions.dto.SumResponse;
import com.example.transactions.dto.TransactionRequest;
import com.example.transactions.dto.TransactionResponse;
import com.example.transactions.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller REST para gestionar transacciones.
 * Responsable de:
 * - Exponer endpoints REST
 * - Validar input básico
 * - Traducir HTTP ⇄ DTO
 * 
 * NO contiene lógica de negocio (delegada al Service).
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * PUT /transactions/{id}
     * Crea o actualiza una transacción.
     * 
     * Body:
     * {
     *   "amount": 100.0,
     *   "type": "cars",
     *   "parentId": 1  // opcional
     * }
     * 
     * Respuesta:
     * {
     *   "status": "ok"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<StatusResponse> createOrUpdateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        transactionService.createOrUpdateTransaction(id, request);
        return ResponseEntity.ok(new StatusResponse("ok"));
    }

    /**
     * GET /transactions/{id}
     * Obtiene una transacción por ID.
     * 
     * Respuesta:
     * {
     *   "id": 10,
     *   "amount": 5000.0,
     *   "type": "cars",
     *   "parentId": null
     * }
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /transactions/types/{type}
     * Obtiene todos los IDs de transacciones de un tipo dado.
     * 
     * Respuesta:
     * [1, 3, 7]
     */
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getTransactionIdsByType(@PathVariable String type) {
        List<Long> ids = transactionService.getTransactionIdsByType(type);
        return ResponseEntity.ok(ids);
    }

    /**
     * GET /transactions/sum/{id}
     * Calcula el monto total de una transacción incluyendo todas sus descendientes.
     * 
     * Respuesta:
     * {
     *   "sum": 205.0
     * }
     */
    @GetMapping("/sum/{id}")
    public ResponseEntity<SumResponse> getTransactionSum(@PathVariable Long id) {
        BigDecimal sum = transactionService.calculateSum(id);
        return ResponseEntity.ok(new SumResponse(sum));
    }
}
