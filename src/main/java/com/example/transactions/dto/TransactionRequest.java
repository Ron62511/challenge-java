package com.example.transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para recibir datos de transacción desde el cliente.
 */
public class TransactionRequest {
    @NotNull(message = "amount es requerido")
    @Positive(message = "amount debe ser positivo")
    private BigDecimal amount;

    @NotNull(message = "type es requerido")
    private String type;

    @JsonProperty("parent_id")
    private Long parentId;

    public TransactionRequest() {
    }

    public TransactionRequest(BigDecimal amount, String type, Long parentId) {
        this.amount = amount;
        this.type = type;
        this.parentId = parentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
