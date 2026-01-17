package com.example.transactions.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad Transaction que representa una transacción financiera.
 * 
 * Nota: Se usa BigDecimal para manejar montos con precisión decimal
 * y evitar problemas de redondeo que ocurren con double/float.
 */
public class Transaction {
    private Long id;
    private BigDecimal amount;
    private String type;
    private Long parentId;

    public Transaction() {
    }

    public Transaction(Long id, BigDecimal amount, String type, Long parentId) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.parentId = parentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
