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

### Arquitectura en Capas (Layered Architecture)

El proyecto sigue estrictamente una **arquitectura en capas** con inspiración **Clean**, pero sin llevarlo a hexagonal puro.

#### Por qué esta arquitectura y no hexagonal completa

- **Es un code challenge, no un sistema enterprise** → No requiere la complejidad adicional de una arquitectura hexagonal completa
- **Spring Boot está naturalmente alineado a capas** → Framework que promueve el uso de capas claras
- **Es fácil de leer, testear y justificar** → Facilita la mantenibilidad y comprensión del código
- **Muestra buen criterio sin complejidad innecesaria** → Balance entre buenas prácticas y simplicidad

### Capas del Sistema

El flujo de datos sigue una arquitectura en capas clara y bien definida:

```
Controller (API)
    ↓
Service (Business Logic)
    ↓
Repository (In-memory persistence)
    ↓
Model (Domain)
```

### Responsabilidades de cada Capa

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

## Documentación de la API (Swagger)

Una vez que la aplicación esté en ejecución, puedes acceder a la documentación interactiva de la API:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

En Swagger UI podrás:
- Ver todos los endpoints disponibles
- Probar los endpoints directamente desde el navegador
- Ver ejemplos de requests y responses
- Explorar los modelos de datos (DTOs)

## Ejemplos de Uso

### Crear una transacción
```bash
curl -X PUT http://localhost:8080/transactions/10 \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "type": "cars"
  }'
```

### Crear una transacción con parent
```bash
curl -X PUT http://localhost:8080/transactions/11 \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000,
    "type": "shopping",
    "parent_id": 10
  }'
```

### Obtener transacción por ID
```bash
curl http://localhost:8080/transactions/10
```

### Obtener IDs por tipo
```bash
curl http://localhost:8080/transactions/types/cars
```

### Calcular suma total
```bash
curl http://localhost:8080/transactions/sum/10
```
