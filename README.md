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

### Principios SOLID Aplicados

El proyecto implementa los principios SOLID para mantener un código limpio, mantenible y extensible:

#### 1. Single Responsibility Principle (SRP) - Principio de Responsabilidad Única

**Cada clase tiene una única razón para cambiar:**

- **`TransactionController`** → Solo responsabilidad: manejar peticiones HTTP y traducir entre HTTP y DTOs
  - ❌ NO contiene lógica de negocio
  - ✅ Solo expone endpoints REST
  - ✅ Valida entrada básica con `@Valid`

- **`TransactionService`** → Solo responsabilidad: lógica de negocio
  - ✅ Valida reglas de negocio (parentId válido, no ciclos, etc.)
  - ✅ Calcula sumas recursivas
  - ❌ NO sabe nada de HTTP ni de persistencia

- **`TransactionRepository`** (interface) + **`TransactionRepositoryImpl`** → Solo responsabilidad: persistencia
  - ✅ Guarda/recupera transacciones en memoria
  - ✅ Mantiene índices para búsquedas eficientes
  - ❌ NO contiene lógica de negocio

- **`GlobalExceptionHandler`** → Solo responsabilidad: manejar excepciones globalmente
  - ✅ Convierte excepciones en respuestas HTTP apropiadas
  - ✅ Mapea excepciones a códigos de estado HTTP

#### 2. Open/Closed Principle (OCP) - Principio Abierto/Cerrado

**Las clases están abiertas para extensión pero cerradas para modificación:**

- **`TransactionRepository` (interface)**:
  - ✅ Permite crear nuevas implementaciones (BD, archivo, etc.) sin modificar el código existente
  - ✅ `TransactionService` puede trabajar con cualquier implementación sin cambios

- **`GlobalExceptionHandler`**:
  - ✅ Permite agregar nuevos `@ExceptionHandler` sin modificar los existentes
  - ✅ Extensible para nuevas excepciones personalizadas

- **DTOs**:
  - ✅ Extensibles sin modificar código existente
  - ✅ Nuevos campos no rompen compatibilidad hacia atrás

#### 3. Liskov Substitution Principle (LSP) - Principio de Sustitución de Liskov

**Las implementaciones pueden ser sustituidas por sus interfaces sin alterar el comportamiento:**

- **`TransactionRepositoryImpl`** puede ser sustituido por cualquier implementación de `TransactionRepository`:
  ```java
  // Service trabaja con la interfaz, no con la implementación
  public TransactionService(TransactionRepository repository) {
      this.repository = repository; // Cualquier implementación funciona
  }
  ```
  - ✅ Permite cambiar de persistencia en memoria a base de datos sin modificar `TransactionService`
  - ✅ Facilita testing con mocks o implementaciones de prueba

#### 4. Interface Segregation Principle (ISP) - Principio de Segregación de Interfaces

**Las interfaces deben ser específicas y cohesivas:**

- **`TransactionRepository`**:
  - ✅ Define solo los métodos necesarios para persistencia de transacciones
  - ✅ No fuerza a implementar métodos innecesarios
  - ✅ Métodos específicos y bien definidos: `findById`, `findByParentId`, `findIdsByType`, etc.

- **DTOs específicos por responsabilidad**:
  - ✅ `TransactionRequest` → Solo para entrada de datos
  - ✅ `TransactionResponse` → Solo para salida de datos
  - ✅ `SumResponse` → Solo para respuesta de suma
  - ✅ `StatusResponse` → Solo para respuesta de estado
  - ❌ NO hay un DTO genérico que mezcle responsabilidades

#### 5. Dependency Inversion Principle (DIP) - Principio de Inversión de Dependencias

**Depende de abstracciones, no de concreciones:**

- **`TransactionController`** depende de `TransactionService` (abstracción):
  ```java
  private final TransactionService transactionService; // Dependencia inyectada
  ```
  - ✅ No conoce la implementación interna del servicio
  - ✅ Spring Boot inyecta la dependencia mediante constructor

- **`TransactionService`** depende de `TransactionRepository` (interface):
  ```java
  private final TransactionRepository repository; // Interfaz, no implementación
  ```
  - ✅ No conoce detalles de implementación de persistencia
  - ✅ Permite cambiar la implementación sin modificar el servicio

- **Inyección de Dependencias (Spring)**:
  - ✅ Todas las dependencias se inyectan por constructor
  - ✅ Facilita testing y desacoplamiento
  - ✅ El código depende de abstracciones (interfaces), no de clases concretas

### Resumen de Principios SOLID en el Código

| Principio | Aplicación | Ubicación |
|-----------|------------|-----------|
| **SRP** | Separación clara de responsabilidades por capa | Controller, Service, Repository, ExceptionHandler |
| **OCP** | Extensible sin modificar código existente | Repository (interface), ExceptionHandler |
| **LSP** | Implementaciones sustituibles | `TransactionRepositoryImpl` implementa `TransactionRepository` |
| **ISP** | Interfaces específicas y cohesivas | `TransactionRepository`, DTOs específicos |
| **DIP** | Dependencias de abstracciones | Inyección de dependencias mediante interfaces |

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

### Opción 1: Ejecución Local (sin Docker)

```bash
# Compilar y ejecutar tests
mvn clean test

# Ejecutar la aplicación
mvn spring-boot:run
```

El servicio estará disponible en `http://localhost:8080`

### Opción 2: Ejecución con Docker

#### Construir y ejecutar con Docker

```bash
# Construir la imagen Docker
docker build -t transactions-api:1.0.0 .

# Ejecutar el contenedor
docker run -d -p 8080:8080 --name transactions-service transactions-api:1.0.0
```

#### Usar Docker Compose (recomendado)

```bash
# Construir y ejecutar con docker-compose
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener el servicio
docker-compose down
```

#### Comandos útiles de Docker

```bash
# Ver logs del contenedor
docker logs -f transactions-service

# Detener el contenedor
docker stop transactions-service

# Iniciar el contenedor
docker start transactions-service

# Eliminar el contenedor
docker rm transactions-service

# Ver el estado del contenedor
docker ps -a

# Inspeccionar la imagen
docker inspect transactions-api:1.0.0
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

## Dockerización

### Características del Dockerfile

El Dockerfile utiliza un **multi-stage build** para optimizar el tamaño de la imagen final:

#### Stage 1: Build
- Usa `maven:3.9-eclipse-temurin-17` como base
- Descarga y cachea dependencias de Maven
- Compila y empaqueta la aplicación en un JAR ejecutable

#### Stage 2: Runtime
- Usa `eclipse-temurin:17-jre-alpine` (imagen ligera)
- Solo contiene el JRE necesario para ejecutar la aplicación
- **Tamaño reducido**: ~150-200MB vs ~500MB+ con imagen completa de Maven

### Optimizaciones Implementadas

✅ **Multi-stage build** → Reduce el tamaño de la imagen final
✅ **Imagen Alpine** → Imagen base más ligera
✅ **Usuario no-root** → Mayor seguridad
✅ **Health check** → Verificación automática del estado de la aplicación
✅ **Cacheo de dependencias Maven** → Builds más rápidos si `pom.xml` no cambia
✅ **Variables de entorno** → Configuración flexible de JVM

### Estructura de Archivos Docker

```
challenge-java/
├── Dockerfile              # Definición de la imagen Docker
├── .dockerignore           # Archivos excluidos del contexto Docker
└── docker-compose.yml      # Orquestación con Docker Compose
```

### Variables de Entorno

Puedes personalizar la ejecución mediante variables de entorno:

```bash
# Levantar servicio con docker
# Construir y levantar con docker-compose
docker-compose up -d --build

# O construir primero y luego levantar
docker-compose build
docker-compose up -d

# Ejecutar en puerto externo diferente (9000 externo, 8080 interno del contenedor)
docker run -d -p 9000:8080 \
  --name transactions-service \
  transactions-api:1.0.0

# Ejecutar con múltiples variables de entorno
docker run -d -p 8080:8080 \
  -e JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC" \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name transactions-service \
  transactions-api:1.0.0
```

**Variables de entorno disponibles:**

- `JAVA_OPTS`: Opciones de la JVM (memoria, garbage collector, etc.)
  - Ejemplo: `-Xmx1024m -Xms512m -XX:+UseG1GC`
- `SERVER_PORT`: Puerto interno del servidor (por defecto: 8080)
- `SPRING_PROFILES_ACTIVE`: Perfil de Spring Boot activo

### Troubleshooting

#### Ver logs de la aplicación
```bash
docker logs -f transactions-service
```

#### Acceder al contenedor
```bash
docker exec -it transactions-service sh
```

#### Verificar estado del health check
```bash
docker inspect --format='{{.State.Health.Status}}' transactions-service
```

#### Reconstruir sin usar caché
```bash
docker build --no-cache -t transactions-api:1.0.0 .
```

###  Detener el service
```bash
# Detener el servicio (mantiene el contenedor)
docker-compose stop

# O detener y eliminar el contenedor completamente
docker-compose down
```
