# SecureBank

A secure, event-driven banking application built using Spring Boot, FastAPI, Apache Kafka, Redis, PostgreSQL, Docker, and JWT Authentication. The application provides secure banking operations while integrating a machine learning–based fraud detection service that evaluates every financial transaction in real time.

---

## Table of Contents

* Project Overview
* Features
* System Architecture
* Technology Stack
* Project Structure

---

# Project Overview

SecureBank is a backend-focused banking application designed to demonstrate modern software engineering practices used in enterprise financial systems. The project combines secure RESTful APIs, event-driven messaging, distributed services, machine learning integration, and containerized deployment into a single application.

The system enables authenticated users to perform banking operations such as deposits, withdrawals, transfers, account management, and profile updates. Every transaction generates an event that is published to Apache Kafka, allowing an independent fraud detection service to evaluate transaction risk without impacting the responsiveness of the primary banking application.

The project emphasizes modular design, separation of concerns, secure authentication, asynchronous processing, and scalable deployment using Docker Compose.

---

# Features

## Authentication and Authorization

* User registration and authentication
* JWT-based stateless authentication
* BCrypt password hashing
* Role-based access control (USER and ADMIN)
* Protected REST endpoints

## User Management

* User profile retrieval
* Profile update functionality
* User activation and deactivation
* Administrative user management

## Account Management

* Savings and current account support
* Account creation
* Account lookup
* Balance inquiry

## Transaction Processing

* Cash deposits
* Cash withdrawals
* Account-to-account transfers
* Transaction history
* Account ownership validation
* Balance validation

## AI Fraud Detection

* Event publication using Apache Kafka
* FastAPI-based fraud detection microservice
* Machine learning prediction pipeline
* Fraud score generation
* Asynchronous transaction analysis

## Performance

* Redis caching
* Asynchronous event processing
* Containerized deployment
* Environment-based configuration

## Security

* Spring Security
* JWT Authentication
* Password encryption
* Input validation
* Global exception handling
* Custom exception hierarchy

---

# System Architecture

```text
                           Client Applications
                        (Postman / Web Frontend)
                                   │
                                   │
                            HTTPS REST APIs
                                   │
                                   ▼
                     ┌───────────────────────────┐
                     │     Spring Boot API       │
                     │      SecureBank Core      │
                     └───────────────────────────┘
                         │          │         │
                         │          │         │
                         ▼          ▼         ▼
                   PostgreSQL     Redis     Kafka
                         │                    │
                         │                    ▼
                         │         Transaction Events
                         │                    │
                         ▼                    ▼
                 Persistent Storage   Fraud Detection Service
                                            (FastAPI)
                                                 │
                                                 ▼
                                       Machine Learning Model
                                                 │
                                                 ▼
                                         Fraud Prediction
```

---

# Technology Stack

| Category             | Technologies                 |
| -------------------- | ---------------------------- |
| Programming Language | Java 21, Python 3.11         |
| Backend Framework    | Spring Boot                  |
| Security             | Spring Security, JWT, BCrypt |
| Database             | PostgreSQL                   |
| ORM                  | Spring Data JPA (Hibernate)  |
| Cache                | Redis                        |
| Message Broker       | Apache Kafka                 |
| Coordination Service | Apache ZooKeeper             |
| AI Service           | FastAPI                      |
| Machine Learning     | Scikit-learn, Pandas, Joblib |
| Build Tool           | Maven                        |
| Containerization     | Docker, Docker Compose       |
| Version Control      | Git, GitHub                  |
| API Testing          | Postman                      |

---

# Project Structure

```text
securebank
│
├── backend
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com.securebank
│   │   │   │       ├── config
│   │   │   │       ├── controller
│   │   │   │       ├── dto
│   │   │   │       ├── entity
│   │   │   │       ├── event
│   │   │   │       ├── exception
│   │   │   │       ├── repository
│   │   │   │       ├── security
│   │   │   │       ├── service
│   │   │   │       └── SecureBankApplication.java
│   │   │   │
│   │   │   └── resources
│   │   │       └── application.properties
│   │   │
│   │   └── test
│   │
│   ├── Dockerfile
│   └── pom.xml
│
├── fraud_detection
│   ├── main.py
│   ├── model_pipeline.pkl
│   ├── requirements.txt
│   └── Dockerfile
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

# Installation

## Prerequisites

Ensure the following software is installed before running the application.

| Software       | Version     |
| -------------- | ----------- |
| Java           | 21 or later |
| Maven          | 3.9+        |
| Python         | 3.11+       |
| Docker         | Latest      |
| Docker Compose | Latest      |
| Git            | Latest      |

---

## Clone the Repository

```bash
git clone https://github.com/<username>/securebank.git

cd securebank
```

---

## Configure Environment Variables

Create a `.env` file in the project root.

```text
DB_USERNAME=postgres
DB_PASSWORD=<your_password>
DB_URL=jdbc:postgresql://postgres:5432/securebank

JWT_SECRET=<your_secret_key>

FRAUD_SERVICE_URL=http://fraud-detection:8000
```

---

## Build the Project

```bash
docker compose up --build
```

The command performs the following tasks:

* Builds the Spring Boot application
* Builds the FastAPI service
* Starts PostgreSQL
* Starts Redis
* Starts Apache Kafka
* Starts Apache ZooKeeper
* Creates the Docker network
* Starts all services

After startup, the backend API will be available at

```
http://localhost:8080
```

The AI Fraud Detection service will be available at

```
http://localhost:8000
```

---

# Docker Deployment

SecureBank is fully containerized using Docker Compose.

The deployment consists of six independent services.

| Service             | Purpose                 |
| ------------------- | ----------------------- |
| Spring Boot Backend | Banking APIs            |
| PostgreSQL          | Persistent data storage |
| Redis               | Caching                 |
| Apache Kafka        | Event streaming         |
| Apache ZooKeeper    | Kafka coordination      |
| FastAPI             | AI Fraud Detection      |

The services communicate through Docker's internal network.

```text
                Docker Network

        ┌──────────────────────────────┐

        Spring Boot Backend
               │
               ├──────── PostgreSQL
               │
               ├──────── Redis
               │
               ├──────── Kafka
               │                │
               │                ▼
               │          FastAPI Service
               │
               ▼
          REST API Clients

        └──────────────────────────────┘
```

To start the complete application

```bash
docker compose up --build
```

To stop all services

```bash
docker compose down
```

To view running containers

```bash
docker ps
```

---

# Environment Variables

The application uses environment variables to avoid storing sensitive configuration directly in source code.

| Variable          | Description                            |
| ----------------- | -------------------------------------- |
| DB_USERNAME       | PostgreSQL username                    |
| DB_PASSWORD       | PostgreSQL password                    |
| DB_URL            | PostgreSQL JDBC URL                    |
| JWT_SECRET        | Secret key used for signing JWT tokens |
| FRAUD_SERVICE_URL | FastAPI service endpoint               |

These values are loaded through Docker Compose and injected into the Spring Boot application during container startup.

Sensitive information such as passwords and secret keys should never be committed to the repository.

---

# REST API

## Authentication

| Method | Endpoint             | Description         |
| ------ | -------------------- | ------------------- |
| POST   | `/api/auth/register` | Register a new user |
| POST   | `/api/auth/login`    | Authenticate user   |

---

## User

| Method | Endpoint            | Description    |
| ------ | ------------------- | -------------- |
| GET    | `/api/user/profile` | View profile   |
| PUT    | `/api/user/profile` | Update profile |

---

## Account

| Method | Endpoint                        | Description        |
| ------ | ------------------------------- | ------------------ |
| POST   | `/api/accounts`                 | Create account     |
| GET    | `/api/accounts`                 | View user accounts |
| GET    | `/api/accounts/{accountNumber}` | Account details    |

---

## Transactions

| Method | Endpoint                            | Description                   |
| ------ | ----------------------------------- | ----------------------------- |
| POST   | `/api/transactions`                 | Deposit / Withdraw / Transfer |
| GET    | `/api/transactions/{accountNumber}` | Transaction history           |

---

## Administration

| Method | Endpoint                       | Description                 |
| ------ | ------------------------------ | --------------------------- |
| GET    | `/api/admin/users`             | List all users              |
| GET    | `/api/admin/users/{id}`        | User details                |
| PUT    | `/api/admin/users/{id}/status` | Activate or deactivate user |
| GET    | `/api/admin/transactions`      | View all transactions       |

---

# AI Fraud Detection Workflow

Every financial transaction follows an event-driven processing pipeline.

```text
Client Request
      │
      ▼
Spring Boot REST API
      │
      ▼
Transaction Validation
      │
      ▼
Database Update
      │
      ▼
Publish Transaction Event
      │
      ▼
Apache Kafka
      │
      ▼
FastAPI Consumer
      │
      ▼
Machine Learning Model
      │
      ▼
Fraud Prediction
      │
      ▼
Response Logging
```

The fraud detection service operates independently of the banking application.

This architecture allows transaction processing to remain responsive while fraud analysis occurs asynchronously.

The machine learning model receives transaction metadata, performs feature preprocessing, generates a fraud probability score, and returns the prediction to the Spring Boot application for logging and future analysis.

# Kafka Event Flow

SecureBank follows an event-driven architecture for transaction processing. Every successful financial transaction generates a Kafka event that is consumed by the AI fraud detection service.

```text
                 Transaction Request
                         │
                         ▼
                Spring Boot Backend
                         │
                         ▼
                Transaction Validation
                         │
                         ▼
                Persist Transaction
                  (PostgreSQL)
                         │
                         ▼
               Publish Kafka Event
             Topic: transaction-events
                         │
                         ▼
                  Apache Kafka Broker
                         │
                         ▼
             FastAPI Kafka Consumer
                         │
                         ▼
             Feature Preprocessing
                         │
                         ▼
          Machine Learning Prediction
                         │
                         ▼
               Fraud Detection Result
```

### Transaction Event

Each event published to Kafka contains the following information.

| Field           | Description                                |
| --------------- | ------------------------------------------ |
| transactionId   | Unique transaction identifier              |
| accountNumber   | Source account                             |
| transactionType | Deposit, Withdrawal or Transfer            |
| amount          | Transaction amount                         |
| email           | User email associated with the transaction |

The event-driven design allows fraud analysis to execute independently of the primary transaction processing workflow, reducing response latency while maintaining scalability.

---

# Security Architecture

Security is implemented using Spring Security together with JWT authentication.

## Authentication Flow

```text
                Login Request
                      │
                      ▼
               Authentication API
                      │
                      ▼
            Username & Password
                  Verification
                      │
                      ▼
              Generate JWT Token
                      │
                      ▼
              Return JWT Token
                      │
                      ▼
         Client Stores Authentication Token
```

For every protected API request:

```text
Client
   │
Authorization: Bearer <JWT>
   │
   ▼
JWT Authentication Filter
   │
   ▼
Token Validation
   │
   ▼
Extract User Details
   │
   ▼
Spring Security Context
   │
   ▼
Protected Controller
```

### Security Features

* JWT-based stateless authentication
* BCrypt password hashing
* Role-based authorization
* Authentication filter for protected endpoints
* Input validation using Jakarta Validation
* Global exception handling
* Environment variable configuration for sensitive data

---

# Exception Handling

The application uses a centralized exception handling mechanism through `@RestControllerAdvice`.

Custom exceptions include:

| Exception                    | Description                                      |
| ---------------------------- | ------------------------------------------------ |
| AccountNotFoundException     | Account does not exist                           |
| UserNotFoundException        | User not found                                   |
| UnauthorizedException        | Unauthorized resource access                     |
| InsufficientBalanceException | Withdrawal or transfer exceeds available balance |

Validation and authentication exceptions are also handled globally to provide consistent HTTP responses.

Example error response:

```json
{
    "message": "Insufficient balance",
    "status": 400,
    "timestamp": "2026-06-28T15:30:15"
}
```

---

# Logging

Application logging is implemented using SLF4J.

The following operations are logged:

* User registration
* User login
* Account creation
* Deposits
* Withdrawals
* Transfers
* Kafka event publication
* Kafka event consumption
* Fraud prediction
* Exception handling

Logging enables request tracing, debugging, and monitoring during development.

---

# Database Design

The application currently consists of three primary entities.

```text
                User
                 │
         One-to-Many
                 │
                 ▼
              Account
                 │
         One-to-Many
                 │
                 ▼
           Transaction
```

## Tables

| Table        | Purpose                    |
| ------------ | -------------------------- |
| users        | Stores registered users    |
| accounts     | Stores account information |
| transactions | Stores transaction history |

Relationships are managed using Spring Data JPA and Hibernate.

---

# Future Improvements

The current implementation establishes a strong backend foundation while leaving room for additional enterprise features.

Potential future enhancements include:

* React or Angular frontend
* Swagger/OpenAPI documentation
* Refresh Token authentication
* Email notifications
* SMS notifications
* Transaction scheduling
* Account statements in PDF format
* Role-based audit logging
* Prometheus and Grafana monitoring
* ELK stack integration
* Kubernetes deployment
* CI/CD pipeline using GitHub Actions
* Unit and integration testing
* Rate limiting
* Multi-factor authentication
* Distributed tracing

---

# License

This project is intended for educational and portfolio purposes.

You may use the source code as a reference for learning modern backend development practices.

---

# Author

**M Hariharan**

Master of Computer Applications (MCA)

Backend Developer | Java | Spring Boot | PostgreSQL | Apache Kafka | Redis | FastAPI | Docker | Machine Learning

GitHub: https://github.com/Hariharan-M-MCA

LinkedIn: https://www.linkedin.com/in/hariharan-m-37768328b/

---

# Acknowledgements

This project was developed to explore enterprise backend development concepts including secure authentication, event-driven architecture, distributed messaging, containerization, and machine learning integration.

It demonstrates the integration of multiple technologies into a production-inspired banking system while following modular software design principles and modern backend development practices.
