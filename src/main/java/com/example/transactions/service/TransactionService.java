package com.example.transactions.service;

import com.example.transactions.dto.TransactionRequest;
import com.example.transactions.dto.TransactionResponse;
import com.example.transactions.exception.DuplicateTransactionException;
import com.example.transactions.exception.InvalidParentException;
import com.example.transactions.exception.TransactionNotFoundException;
import com.example.transactions.model.Transaction;
import com.example.transactions.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio que contiene toda la lógica de negocio para transacciones.
 */
@Service
public class TransactionService {

    private final TransactionRepository repository;

    @Autowired
    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Crea o actualiza una transacción.
     * Valida:
     * - No permite IDs duplicados (excepto al actualizar la misma transacción)
     * - parentId debe apuntar a una transacción válida si se proporciona
     * - Evita ciclos en la jerarquía
     */
    public TransactionResponse createOrUpdateTransaction(Long id, TransactionRequest request) {
        // Validar que el ID no sea nulo
        if (id == null) {
            throw new IllegalArgumentException("El ID de la transacción no puede ser nulo");
        }

        // Si la transacción ya existe, verificar si es una actualización válida
        Transaction existing = repository.findById(id);
        boolean isUpdate = existing != null;

        // Validar parentId si se proporciona
        if (request.getParentId() != null) {
            validateParentId(id, request.getParentId(), isUpdate);
        }

        // Crear la transacción
        Transaction transaction = new Transaction(
                id,
                request.getAmount(),
                request.getType(),
                request.getParentId()
        );

        // Guardar la transacción
        repository.save(transaction);

        // Convertir a DTO de respuesta
        return toResponse(transaction);
    }

    /**
     * Obtiene una transacción por ID.
     */
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = repository.findById(id);
        if (transaction == null) {
            throw new TransactionNotFoundException("Transacción con ID " + id + " no encontrada");
        }
        return toResponse(transaction);
    }

    /**
     * Obtiene todos los IDs de transacciones de un tipo dado.
     */
    public List<Long> getTransactionIdsByType(String type) {
        return repository.findIdsByType(type);
    }

    /**
     * Calcula el monto total de una transacción incluyendo todas sus descendientes.
     * Usa un algoritmo DFS recursivo para recorrer la jerarquía.
     */
    public BigDecimal calculateSum(Long id) {
        Transaction transaction = repository.findById(id);
        if (transaction == null) {
            throw new TransactionNotFoundException("Transacción con ID " + id + " no encontrada");
        }

        // Usar un Set para evitar procesar la misma transacción dos veces
        // (por si hubiera algún ciclo que no detectamos)
        Set<Long> visited = new HashSet<>();
        return calculateSumRecursive(id, visited);
    }

    /**
     * Método recursivo para calcular la suma de una transacción y sus descendientes.
     */
    private BigDecimal calculateSumRecursive(Long id, Set<Long> visited) {
        // Prevenir ciclos infinitos
        if (visited.contains(id)) {
            return BigDecimal.ZERO;
        }
        visited.add(id);

        Transaction transaction = repository.findById(id);
        if (transaction == null) {
            return BigDecimal.ZERO;
        }

        // Suma del monto actual
        BigDecimal sum = transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO;

        // Sumar todos los montos de las transacciones hijas
        List<Transaction> children = repository.findByParentId(id);
        for (Transaction child : children) {
            sum = sum.add(calculateSumRecursive(child.getId(), visited));
        }

        return sum;
    }

    /**
     * Valida que el parentId sea válido y no cree ciclos.
     */
    private void validateParentId(Long transactionId, Long parentId, boolean isUpdate) {
        // El parentId no puede ser el mismo que el ID de la transacción
        if (transactionId.equals(parentId)) {
            throw new InvalidParentException(
                    "Una transacción no puede ser su propio padre. ID: " + transactionId
            );
        }

        // El parentId debe apuntar a una transacción existente
        Transaction parent = repository.findById(parentId);
        if (parent == null) {
            throw new InvalidParentException(
                    "La transacción padre con ID " + parentId + " no existe"
            );
        }

        // Verificar que no se creen ciclos
        // Si el parentId ya tiene como ancestro al transactionId, se crearía un ciclo
        if (wouldCreateCycle(transactionId, parentId)) {
            throw new InvalidParentException(
                    "Asignar parentId " + parentId + " a la transacción " + transactionId +
                            " crearía un ciclo en la jerarquía"
            );
        }
    }

    /**
     * Verifica si asignar un parentId crearía un ciclo en la jerarquía.
     */
    private boolean wouldCreateCycle(Long transactionId, Long parentId) {
        Set<Long> visited = new HashSet<>();
        Long currentId = parentId;

        // Recorrer hacia arriba en la jerarquía
        while (currentId != null) {
            // Si encontramos el transactionId en la cadena de ancestros, hay un ciclo
            if (currentId.equals(transactionId)) {
                return true;
            }

            // Si ya visitamos este nodo, hay un ciclo
            if (visited.contains(currentId)) {
                return true;
            }

            visited.add(currentId);

            // Obtener el padre de la transacción actual
            Transaction current = repository.findById(currentId);
            if (current == null) {
                break;
            }

            currentId = current.getParentId();
        }

        return false;
    }

    /**
     * Convierte una entidad Transaction a TransactionResponse.
     */
    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getParentId()
        );
    }
}
