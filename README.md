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
+-- db/migration/V2__create_users_table.sql
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
- `APP_WEB_CORS_ALLOWED_ORIGINS` (default `http://localhost:5173`)
- `APP_AUTH_JWT_SECRET` (default local development secret in `application.properties`)
- `APP_AUTH_JWT_ACCESS_TOKEN_TTL` (default `PT24H`)

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

### 7.2 Login
- Method: `POST`
- Path: `/auth/login`
- Public endpoint
- Request body:
  - `email`
  - `password`
- Success response: `200 OK` JSON
  - `accessToken`
  - `expiresAt`
- Failure response: `401 Unauthorized` JSON
  - `message`: `Invalid credentials`

Token policy:
- Access tokens are signed JWTs.
- Default expiration is `24 hours` (`PT24H`).
- Expired tokens are rejected by the token validation service.

Example:
```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"person@example.com","password":"secret123"}'
```

### 7.3 Authenticated Identity
- Method: `GET`
- Path: `/me`
- Protected endpoint
- Requires `Authorization: Bearer <access-token>`
- Success response: `200 OK` JSON
  - `userId`
  - `email`
- Failure response: `401 Unauthorized` JSON
  - `message`: `Unauthorized`

Authentication enforcement:
- `/health` remains public.
- `/auth/login` remains public.
- Protected routes reject missing, invalid, and expired tokens with the same generic `401` response.
- Valid tokens attach authenticated user identity to the request context for downstream use.

## 8. Authentication Persistence Foundation
- Table: `users`
- Columns:
  - `id` (`UUID`, primary key)
  - `email` (`VARCHAR(320)`, required, unique)
  - `password_hash` (`VARCHAR(255)`, required)
  - `created_at` (`TIMESTAMP`, required, defaults to current timestamp)

Email normalization strategy:
- Emails are normalized to trimmed lowercase before entering the domain model.
- Database storage is enforced as lowercase with `CHECK (email = LOWER(email))`.
- Uniqueness is enforced on the normalized stored value through a unique index on `email`.

Why this matters:
- This removes ambiguity around case sensitivity and keeps authentication persistence compatible with future shared-account ownership.

## 9. Frontend Routes
- `/login`: email/password login page with client-side validation and generic auth failure handling
- `/app`: post-login app shell route, guarded by local access-token presence

MVP auth flow:
- Login submits to `POST /auth/login`
- On success, the frontend stores `accessToken` in browser `localStorage` under `nexo.accessToken`
- The app redirects to `/app`
- On failure, the UI shows only `Invalid credentials`

## 10. PWA Installability
Implemented foundation:
- `manifest.webmanifest` with app metadata and icons
- service worker (`public/sw.js`) registration in `src/main.tsx`
- standalone display mode and mobile metadata in `index.html`

Validation checklist:
- App loads in browser
- Browser detects manifest/service worker
- "Add to Home Screen" appears on supported mobile browsers

## 11. Testing and Build
### 11.1 Backend tests
```bash
./mvnw -q test
```

### 11.2 Frontend build
```bash
cd frontend
npm run build
```

## 12. CI
Workflow file:
- `.github/workflows/ci.yml`

Current CI validates backend tests with PostgreSQL + Flyway migrations.

## 13. Quality and Contribution Rules
This repository enforces agent rules in `AGENTS.md`.

Mandatory documentation rule:
- Every feature must update `README.md` in the same PR.
- Feature work is incomplete if README documentation is not updated.

## 14. Feature Changelog (Portfolio)
> This section must be updated for every feature.

### 2026-03-06 - User Persistence Foundation (FOUNDATION)
- Added a `users` table migration with `email`, `password_hash`, `created_at`, and UUID primary key.
- Enforced lowercase email storage with a database check and unique index on the normalized stored value.
- Added domain `User`, `EmailAddress`, and `PasswordHash` models to make the normalization and hashed-password contract explicit in code.
- Added tests covering migration creation, duplicate-email rejection, and lowercase email enforcement.
- Why it matters: establishes the minimum durable identity model needed for authentication and future shared-account ownership.

### 2026-03-06 - Login Endpoint MVP (FOUNDATION)
- Added `POST /auth/login` to validate credentials and return a signed access token with expiration metadata.
- Implemented BCrypt password verification, a JPA-backed user lookup adapter, and JWT token issuance/validation infrastructure.
- Standardized login failures to `401` with the generic message `Invalid credentials`.
- Defined a default access-token expiration policy of `24 hours`.
- Why it matters: provides the first secure session-start mechanism needed before protecting finance features behind authentication.

### 2026-03-06 - Protected Route Authentication Enforcement (FOUNDATION)
- Added centralized bearer-token enforcement for protected routes while keeping `/health` and `/auth/login` public.
- Added `GET /auth/me` as a minimal protected endpoint that exposes the authenticated user identity from the request context.
- Standardized protected-route auth failures to `401` with the generic message `Unauthorized` for missing, invalid, and expired tokens.
- Propagated authenticated user identity through the request context for future audit and workspace authorization logic.
- Why it matters: turns token issuance into actual access control and creates the extension point for future authorization rules.

### 2026-03-05 - Frontend PWA Foundation (FOUNDATION)
- Created `frontend/` app with React + TypeScript + Vite.
- Added placeholder routes for `/login` and `/app` with a mobile-first shell.
- Added environment-based API URL through `VITE_API_BASE_URL`, surfaced in placeholder pages for runtime verification.
- Added installable PWA base with manifest, icons, service worker registration, and mobile web-app metadata.
- Why it matters: validates mobile-first runtime assumptions and installability early, preventing rework before auth and dashboard epics.

### 2026-03-06 - Frontend Foundation Validation Pass (FOUNDATION)
- Replaced the Vite starter screen with route-based placeholders for `/login` and `/app`.
- Added explicit `src/config/env.ts` handling for `VITE_API_BASE_URL` with a local default of `http://localhost:8080`.
- Kept the frontend installable through the existing manifest and service worker while tightening mobile metadata in `index.html`.
- Why it matters: moves the frontend from scaffold state to a usable mobile-first foundation that can be validated locally in a browser and on mobile devices.

### 2026-03-06 - Centralized Frontend HTTP Client (FOUNDATION)
- Added a reusable HTTP client layer in `frontend/src/api/httpClient.ts` as the single entry point for backend requests.
- Standardized error handling through an `ApiError` type and shared request pipeline.
- Added `frontend/src/api/healthApi.ts` so feature modules call small API wrappers instead of raw HTTP primitives.
- Prepared the client for future token injection by isolating request header construction in one place.
- Why it matters: prevents scattered networking logic and makes future authentication changes additive instead of invasive.

### 2026-03-06 - Frontend Login MVP (FOUNDATION)
- Replaced the `/login` placeholder with a real email/password form that validates required fields and basic email format before submission.
- Added a frontend auth API wrapper plus local token storage under `nexo.accessToken`.
- Wired centralized bearer-token injection into the shared HTTP client and guarded `/app` behind token presence.
- Standardized failed login UX to the generic message `Invalid credentials` without exposing backend internals.
- Why it matters: completes the first usable session-start flow so portfolio reviewers can see the frontend and backend authentication path working end to end.

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

### 2026-03-06 - Local Frontend CORS Support (FOUNDATION)
- Added backend CORS configuration so the local frontend origin can call API endpoints from the browser.
- Introduced `APP_WEB_CORS_ALLOWED_ORIGINS` for environment-driven origin control, defaulting to `http://localhost:5173`.
- Added a backend test that verifies `GET /health` responds with the expected CORS header for the frontend origin.
- Why it matters: allows the mobile-first frontend to call the backend directly during local development without browser cross-origin failures.

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

