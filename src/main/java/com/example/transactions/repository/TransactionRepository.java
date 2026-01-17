package com.example.transactions.repository;

import com.example.transactions.model.Transaction;

import java.util.List;

/**
 * Interfaz del repositorio para transacciones.
 */
public interface TransactionRepository {
    
    /**
     * Guarda o actualiza una transacción.
     */
    void save(Transaction transaction);

    /**
     * Busca una transacción por ID.
     */
    Transaction findById(Long id);

    /**
     * Verifica si existe una transacción con el ID dado.
     */
    boolean existsById(Long id);

    /**
     * Obtiene todos los IDs de transacciones de un tipo dado.
     */
    List<Long> findIdsByType(String type);

    /**
     * Obtiene todas las transacciones hijas de un parentId dado.
     */
    List<Transaction> findByParentId(Long parentId);

    /**
     * Obtiene todas las transacciones almacenadas.
     */
    List<Transaction> findAll();
}
