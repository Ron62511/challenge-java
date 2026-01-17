# challenge-java

API Transactions - Servicio REST en Java con Spring Boot

## Estructura del Proyecto

```
challenge-java/
├── pom.xml
│
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── example/
    │               └── transactions/
    │                   ├── TransactionsApplication.java
    │                   │
    │                   ├── controller/
    │                   │   └── TransactionController.java
    │                   │
    │                   ├── service/
    │                   │   └── TransactionService.java
    │                   │
    │                   ├── repository/
    │                   │   ├── TransactionRepository.java
    │                   │   └── TransactionRepositoryImpl.java
    │                   │
    │                   ├── model/
    │                   │   └── Transaction.java
    │                   │
    │                   ├── dto/
    │                   │   ├── TransactionRequest.java
    │                   │   ├── TransactionResponse.java
    │                   │   ├── SumResponse.java
    │                   │   └── StatusResponse.java
    │                   │
    │                   └── exception/
    │                       ├── TransactionNotFoundException.java
    │                       ├── DuplicateTransactionException.java
    │                       ├── InvalidParentException.java
    │                       └── GlobalExceptionHandler.java
    │
    └── test/
        └── java/
            └── com/
                └── example/
                    └── transactions/
                        └── service/
                            └── TransactionServiceTest.java
```

## Arquitectura en Capas

El proyecto sigue estrictamente una arquitectura en capas:

- **Controller** → Expone endpoints REST y traduce HTTP ⇄ DTO
- **Service** → Contiene toda la lógica de negocio y validaciones
- **Repository** → Implementa persistencia en memoria (ConcurrentHashMap)
- **Model** → Entidad Transaction sin anotaciones JPA
- **DTO** → Objetos de transferencia de datos
- **Exception** → Excepciones personalizadas y manejo global

## Endpoints REST

### Crear/Actualizar Transacción
```
PUT /transactions/{id}
Content-Type: application/json

{
  "amount": 100.0,
  "type": "cars",
  "parent_id": 1  // opcional
}
```

### Obtener Transacción por ID
```
GET /transactions/{id}
```

### Obtener IDs por Tipo
```
GET /transactions/types/{type}
Respuesta: [1, 3, 7]
```

### Calcular Suma Total
```
GET /transactions/sum/{id}
Respuesta: { "sum": 205.0 }
```

## Ejecución

```bash
# Compilar y ejecutar tests
mvn clean test

# Ejecutar la aplicación
mvn spring-boot:run
```

El servicio estará disponible en `http://localhost:8080`
