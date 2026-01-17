# challenge-java
Api Transactions

Estructura de proyecto sugerida
transaction-service
├── src
│   └── main
│       └── java
│           └── com.example.transactions
│               ├── TransactionServiceApplication.java
│               │
│               ├── controller
│               │   └── TransactionController.java
│               │
│               ├── service
│               │   ├── TransactionService.java
│               │   └── TransactionServiceImpl.java
│               │
│               ├── repository
│               │   └── TransactionRepository.java
│               │
│               ├── model
│               │   └── Transaction.java
│               │
│               ├── dto
│               │   ├── TransactionRequest.java
│               │   └── TransactionResponse.java
│               │
│               └── exception
│                   ├── TransactionNotFoundException.java
│                   └── GlobalExceptionHandler.java
│
│   └── test
│       └── java
│           └── com.example.transactions
│               ├── service
│               │   └── TransactionServiceTest.java
│               └── controller
│                   └── TransactionControllerTest.java
│
└── pom.xml

