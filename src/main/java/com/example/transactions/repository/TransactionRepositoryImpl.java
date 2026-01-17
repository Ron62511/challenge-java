package com.example.transactions.repository;

import com.example.transactions.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del repositorio para transacciones.
 * Usa ConcurrentHashMap para garantizar thread-safety.
 */
@Repository
public class TransactionRepositoryImpl implements TransactionRepository {
    
    // Almacén principal de transacciones por ID
    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    
    // Índice inverso: tipo -> conjunto de IDs de transacciones
    private final Map<String, Set<Long>> typeIndex = new ConcurrentHashMap<>();

    /**
     * Guarda o actualiza una transacción.
     */
    @Override
    public void save(Transaction transaction) {
        Long id = transaction.getId();
        Transaction existing = transactions.get(id);
        
        // Si existe una transacción anterior, removemos su tipo del índice
        if (existing != null) {
            String oldType = existing.getType();
            typeIndex.getOrDefault(oldType, ConcurrentHashMap.newKeySet()).remove(id);
        }
        
        // Guardamos la nueva transacción
        transactions.put(id, transaction);
        
        // Actualizamos el índice por tipo
        typeIndex.computeIfAbsent(transaction.getType(), k -> ConcurrentHashMap.newKeySet())
                .add(id);
    }

    /**
     * Busca una transacción por ID.
     */
    @Override
    public Transaction findById(Long id) {
        return transactions.get(id);
    }

    /**
     * Verifica si existe una transacción con el ID dado.
     */
    @Override
    public boolean existsById(Long id) {
        return transactions.containsKey(id);
    }

    /**
     * Obtiene todos los IDs de transacciones de un tipo dado.
     */
    @Override
    public List<Long> findIdsByType(String type) {
        Set<Long> ids = typeIndex.getOrDefault(type, ConcurrentHashMap.newKeySet());
        return new ArrayList<>(ids);
    }

    /**
     * Obtiene todas las transacciones hijas de un parentId dado.
     */
    @Override
    public List<Transaction> findByParentId(Long parentId) {
        return transactions.values().stream()
                .filter(t -> parentId.equals(t.getParentId()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las transacciones almacenadas.
     */
    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions.values());
    }
}
