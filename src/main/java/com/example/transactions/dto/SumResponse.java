package com.example.transactions.dto;

import java.math.BigDecimal;

/**
 * DTO para la respuesta del endpoint de suma de transacciones.
 */
public class SumResponse {
    private BigDecimal sum;

    public SumResponse() {
    }

    public SumResponse(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }
}
