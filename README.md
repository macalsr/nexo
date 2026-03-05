# Nexo API Documentation

## 1. Overview
Nexo is a backend API built with Java 21 and Spring Boot. This foundation delivers a predictable local/CI setup with PostgreSQL + Flyway, so future features can be built on a stable baseline.

## 2. Foundation Epic Delivered
- Spring Boot API bootstrapped with Web, Validation, JPA, Flyway, and Actuator.
- Local PostgreSQL via Docker Compose.
- Flyway versioned schema migration applied at startup.
- Health endpoint available at `GET /health`.
- CI workflow provisions PostgreSQL and runs tests with migrations applied.

## 3. Tech Stack
- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Flyway
- PostgreSQL 15 (Docker)
- Maven Wrapper (`./mvnw`)

## 4. Architecture Direction
Target architecture (hexagonal):
- `domain`: business rules and invariants.
- `application`: use cases and ports.
- `adapters`: inbound REST and outbound persistence/external clients.
- `infrastructure`: framework wiring.

## 5. Repository Structure
```text
.github/workflows/
+-- ci.yml

docker/
+-- docker-compose.yml

src/main/java/com/mariaribeiro/nexo
+-- NexoApplication.java
+-- api/
    +-- HealthController.java

src/main/resources
+-- application.properties
+-- application-local.properties
+-- db/migration/
    +-- V1__create_app_metadata.sql

src/test/resources
+-- application.properties

src/test/java/com/mariaribeiro/nexo
+-- NexoApplicationTests.java
```

## 6. Prerequisites
- JDK 21
- Docker + Docker Compose

## 7. Local Development
### 7.1 Start only PostgreSQL
```bash
docker compose -f docker/docker-compose.yml up -d postgres
```

### 7.2 Run API on host (recommended for development)
```bash
./mvnw spring-boot:run
```

### 7.3 One-command dev environment (Postgres + API in containers)
```bash
docker compose -f docker/docker-compose.yml --profile app up --build
```

### 7.4 Stop containers
```bash
docker compose -f docker/docker-compose.yml down
```

## 8. Configuration Profiles
### 8.1 `local` profile
File: `src/main/resources/application-local.properties`
- Uses PostgreSQL via env vars:
  - `DB_HOST` (default: `localhost`)
  - `DB_PORT` (default: `5432`)
  - `DB_NAME` (default: `nexo`)
  - `DB_USER` (default: `nexo`)
  - `DB_PASSWORD` (default: `nexo`)
- Flyway enabled and runs automatically on startup.

### 8.2 test configuration
File: `src/test/resources/application.properties`
- Defaults to H2 for fast local tests.
- Can be switched to PostgreSQL via `TEST_DB_*` and `TEST_FLYWAY_*` env vars.

## 9. Database Migration (Flyway)
Migration files:
- `src/main/resources/db/migration/V1__create_app_metadata.sql`

Behavior:
- Flyway runs before application startup completes.
- Schema history tracked by Flyway.

## 10. API Contract
### 10.1 Health
- Method: `GET`
- Path: `/health`
- Response: `200 OK`
- Body: `OK`

Example:
```bash
curl -i http://localhost:8080/health
```

## 11. Testing
### 11.1 Run all tests
```bash
./mvnw -q test
```

### 11.2 Migration proof test
`NexoApplicationTests` checks that Flyway migration created table `app_metadata`.

## 12. CI
Workflow file:
- `.github/workflows/ci.yml`

Pipeline behavior:
- Starts PostgreSQL 15 service.
- Exposes test datasource via env vars.
- Runs `./mvnw -q test`.
- Validates application context + Flyway migration execution.

## 13. Quality and Contribution Rules
This repository enforces agent rules in `AGENTS.md`.

Mandatory documentation rule:
- Every feature must update `README.md` in the same PR.
- Feature work is incomplete if README documentation is not updated.

## 14. Feature Changelog (Portfolio)
> This section must be updated for every feature.

### 2026-03-05 - Setup Spring Boot + Docker + Flyway (FOUNDATION)
- Added `local` runtime config with PostgreSQL + automatic Flyway migration.
- Added baseline migration `V1__create_app_metadata.sql`.
- Added Docker Compose setup for Postgres and optional API container profile.
- Added CI workflow using PostgreSQL service and tests that verify migration output.
- Why it matters: establishes a deterministic backend foundation that scales for future epics and demonstrates production-minded setup in portfolio reviews.

### 2026-03-05 - README Documentation Standard
- Created a structured, documentation-first README.
- Added clear operational sections (setup, run, tests, API contract, configuration).
- Why it matters: improves technical communication and makes the project reviewable for portfolio and hiring contexts.

## 15. Feature Entry Template
Use this template in future PRs:

```md
### YYYY-MM-DD - Feature Name
- What changed: endpoint/use case/domain behavior.
- API contract: method/path/request/response.
- Operational impact: setup, env vars, migrations, or commands changed.
- Testing: what was added/updated.
- Why it matters: portfolio-oriented outcome in one sentence.
```
