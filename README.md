# F1 Betting Service

A resilient Spring Boot service for Formula 1 betting. The system integrates data from the **OpenF1 API** and persists it locally to support betting flows with strong data integrity and high availability.

## Overview

This project exposes REST endpoints to:
- List Formula 1 sessions/events
- Provide a driver market (odds) per event
- Place bets and manage user balances
- Simulate event outcomes and settle bets

The system uses a background synchronization strategy so the database is the **source of truth** for betting operations.

## Key Features

- **Background synchronization** of events and drivers from OpenF1.
- **Database as Source of Truth** to ensure betting operations remain consistent even if the external provider is slow/unavailable.
- **Betting engine** with balance checks, odds, and settlement.
- **Modern DTOs using Java Records** for immutable request/response contracts.
- **Consistent error responses** via a structured `ErrorResponse` model.

## Architecture Notes

### Background Sync (Eventual Consistency)
Instead of calling OpenF1 on every request, the application periodically syncs sessions/drivers and stores them locally. This reduces dependency on third-party uptime and protects core betting flows.

### Why persist external data locally?
- **Integrity:** bets reference stable local entities.
- **Auditability:** historical bets remain valid even if the upstream API changes.
- **Performance:** reads are served from the database.

### DTOs as Records
Request/response DTOs are modeled as Java records to enforce immutability and reduce boilerplate.

## Tech Stack
- Java 21
- Spring Boot 3.2.x (Web, Validation, Data JPA, Scheduling)
- PostgreSQL (dev/prod)
- Flyway (migrations)
- Swagger / OpenAPI
- JUnit 5 + Mockito + MockMvc (tests)

## Running Locally

### With Docker
```bash
  docker-compose up --build
```

The API will be available at:
- `http://localhost:8080`

Swagger UI:
- `http://localhost:8080/swagger-ui.html`

### With Maven
```bash
  ./mvnw spring-boot:run
```

## API Endpoints (High Level)

### Events
- `GET /api/events` — list events (filters supported)
- `GET /api/events/{id}` — get event details

### Bets
- `POST /api/bets` — place a bet

### Users
- `GET /api/users/{id}/balance` — get user balance

### Settlement / Simulation
- `POST /api/events/{id}/outcome` — simulate outcome and settle bets

> For the exact request/response schemas and examples, use Swagger UI.

## Error Handling
Errors are returned using a consistent JSON structure (e.g., `ErrorResponse`) including status, message, and optional field-level validation details.

## Testing
Run the full test suite:
```bash
  ./mvnw test
```

Integration tests cover core flows (bet placement, validations, settlement) using MockMvc.

## Future Improvements
- Authentication/Authorization (JWT)
- Redis cache for high-traffic read endpoints
- Message queue for async settlement processing (Kafka/RabbitMQ)
- Observability: metrics + tracing (Micrometer/Prometheus/Grafana)

