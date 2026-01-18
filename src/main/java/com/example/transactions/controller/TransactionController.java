package com.example.transactions.controller;

import com.example.transactions.dto.StatusResponse;
import com.example.transactions.dto.SumResponse;
import com.example.transactions.dto.TransactionRequest;
import com.example.transactions.dto.TransactionResponse;
import com.example.transactions.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "Transactions", description = "API para gestión de transacciones con soporte de jerarquías parent-child")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * PUT /transactions/{id}
     * Crea o actualiza una transacción.
     */
    @Operation(
            summary = "Crear o actualizar transacción",
            description = "Crea una nueva transacción o actualiza una existente. El parent_id es opcional y debe apuntar a una transacción válida."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transacción creada/actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class),
                            examples = @ExampleObject(value = "{\"status\": \"ok\"}"))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o parent_id no existe"),
            @ApiResponse(responseCode = "409", description = "ID duplicado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StatusResponse> createOrUpdateTransaction(
            @Parameter(description = "ID de la transacción", required = true, example = "10")
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        transactionService.createOrUpdateTransaction(id, request);
        return ResponseEntity.ok(new StatusResponse("ok"));
    }

    /**
     * GET /transactions/{id}
     * Obtiene una transacción por ID.
     */
    @Operation(
            summary = "Obtener transacción por ID",
            description = "Retorna los datos de una transacción específica identificada por su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transacción encontrada",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "ID de la transacción", required = true, example = "10")
            @PathVariable Long id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /transactions/types/{type}
     * Obtiene todos los IDs de transacciones de un tipo dado.
     */
    @Operation(
            summary = "Obtener IDs por tipo",
            description = "Retorna una lista con todos los IDs de transacciones que tienen el tipo especificado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de IDs encontrada",
                    content = @Content(schema = @Schema(implementation = List.class),
                            examples = @ExampleObject(value = "[1, 3, 7]"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getTransactionIdsByType(
            @Parameter(description = "Tipo de transacción", required = true, example = "cars")
            @PathVariable String type) {
        List<Long> ids = transactionService.getTransactionIdsByType(type);
        return ResponseEntity.ok(ids);
    }

    /**
     * GET /transactions/sum/{id}
     * Calcula el monto total de una transacción incluyendo todas sus descendientes.
     */
    @Operation(
            summary = "Calcular suma total",
            description = "Calcula el monto total de una transacción incluyendo el monto de la transacción misma " +
                    "y el monto de todas sus transacciones hijas (recursivamente)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suma calculada exitosamente",
                    content = @Content(schema = @Schema(implementation = SumResponse.class),
                            examples = @ExampleObject(value = "{\"sum\": 205.0}"))),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/sum/{id}")
    public ResponseEntity<SumResponse> getTransactionSum(
            @Parameter(description = "ID de la transacción", required = true, example = "10")
            @PathVariable Long id) {
        BigDecimal sum = transactionService.calculateSum(id);
        return ResponseEntity.ok(new SumResponse(sum));
    }
}
