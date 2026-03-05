# Nexo Platform Documentation

## 1. Overview
Nexo is composed of:
- Backend API (`Spring Boot`, Java 21)
- Frontend app (`React + Vite`, mobile-first PWA foundation)

This repository now validates both backend reliability and mobile-first installability early.

## 2. Tech Stack
### Backend
- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Flyway
- PostgreSQL 15 (Docker)
- Maven Wrapper (`./mvnw`)

### Frontend
- React 19 + TypeScript
- Vite 6.4.1
- React Router DOM
- PWA manifest + service worker

## 3. Repository Structure
```text
.github/workflows/
└── ci.yml

docker/
└── docker-compose.yml

frontend/
|-- src/
|   |-- config/env.ts
|   |-- pages/LoginPage.tsx
|   |-- pages/AppPage.tsx
|   `-- ...
|-- public/
|   |-- manifest.webmanifest
|   |-- sw.js
|   `-- icons/
`-- .env.example

src/main/java/com/mariaribeiro/nexo
+-- NexoApplication.java
+-- api/HealthController.java

src/main/resources
+-- application.properties
+-- application-local.properties
+-- db/migration/V1__create_app_metadata.sql
```
## 4. Local Development
### 4.1 Backend prerequisites
- JDK 21
- Docker + Docker Compose

### 4.2 Start backend database
```bash
docker compose -f docker/docker-compose.yml up -d postgres
```

### 4.3 Run backend
```bash
./mvnw spring-boot:run
```

### 4.4 Run frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend default URL: `http://localhost:5173`

## 5. Backend Configuration
### 5.1 `local` profile
File: `src/main/resources/application-local.properties`
- `DB_HOST` (default `localhost`)
- `DB_PORT` (default `5432`)
- `DB_NAME` (default `nexo`)
- `DB_USER` (default `nexo`)
- `DB_PASSWORD` (default `nexo`)

### 5.2 Test profile
File: `src/test/resources/application.properties`
- Defaults to H2
- Can target PostgreSQL through `TEST_DB_*` and `TEST_FLYWAY_*`

## 6. Frontend Configuration
File: `frontend/.env.example`
- `VITE_API_BASE_URL=http://localhost:8080`

Usage:
- Vite injects values from `.env` files into `import.meta.env`
- App reads this from `src/config/env.ts`

## 7. Backend API Contract
### 7.1 Health
- Method: `GET`
- Path: `/health`
- Public endpoint (no auth)
- Response: `200 OK` JSON
  - `status`: `UP`
  - `service`: `nexo-api`
  - `timestamp`: ISO-8601 UTC datetime

Example:
```bash
curl -i http://localhost:8080/health
```

## 8. Frontend Routes
- `/login`: login placeholder screen
- `/app`: app placeholder screen

Both routes exist to validate navigation structure before auth and final UI implementation.

## 9. PWA Installability
Implemented foundation:
- `manifest.webmanifest` with app metadata and icons
- service worker (`public/sw.js`) registration in `src/main.tsx`
- standalone display mode and mobile metadata in `index.html`

Validation checklist:
- App loads in browser
- Browser detects manifest/service worker
- "Add to Home Screen" appears on supported mobile browsers

## 10. Testing and Build
### 10.1 Backend tests
```bash
./mvnw -q test
```

### 10.2 Frontend build
```bash
cd frontend
npm run build
```

## 11. CI
Workflow file:
- `.github/workflows/ci.yml`

Current CI validates backend tests with PostgreSQL + Flyway migrations.

## 12. Quality and Contribution Rules
This repository enforces agent rules in `AGENTS.md`.

Mandatory documentation rule:
- Every feature must update `README.md` in the same PR.
- Feature work is incomplete if README documentation is not updated.

## 13. Feature Changelog (Portfolio)
> This section must be updated for every feature.

### 2026-03-05 - Frontend PWA Foundation (FOUNDATION)
- Created `frontend/` app with React + TypeScript + Vite.
- Added placeholder routes for `/login` and `/app`.
- Added environment-based API URL through `VITE_API_BASE_URL`.
- Added installable PWA base with manifest, icons, and service worker registration.
- Why it matters: validates mobile-first runtime assumptions and prevents rework before auth/UI epics.

### 2026-03-05 - Setup Spring Boot + Docker + Flyway (FOUNDATION)
- Added `local` runtime config with PostgreSQL + automatic Flyway migration.
- Added baseline migration `V1__create_app_metadata.sql`.
- Added Docker Compose setup for Postgres and optional API container profile.
- Added CI workflow using PostgreSQL service and tests that verify migration output.
- Why it matters: establishes a deterministic backend foundation that scales for future epics and demonstrates production-minded setup in portfolio reviews.

### 2026-03-05 - Health Endpoint Contract (FOUNDATION)
- Updated `GET /health` to return structured JSON for monitoring compatibility.
- Added HTTP controller test validating `200`, JSON response type, and health fields.
- Endpoint remains public (no authentication requirement).
- Why it matters: improves fast runtime validation for development and future hosting checks.

### 2026-03-05 - README Documentation Standard
- Created a structured, documentation-first README.
- Added clear operational sections (setup, run, tests, API contract, configuration).
- Why it matters: improves technical communication and makes the project reviewable for portfolio and hiring contexts.

## 14. Feature Entry Template
Use this template in future PRs:

```md
### YYYY-MM-DD - Feature Name
- What changed: endpoint/use case/domain behavior.
- API contract: method/path/request/response.
- Operational impact: setup, env vars, migrations, or commands changed.
- Testing: what was added/updated.
- Why it matters: portfolio-oriented outcome in one sentence.
```

