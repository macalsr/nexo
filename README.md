# Nexo

Nexo is a full-stack authentication foundation built with a Spring Boot API and a React frontend.

## Why this project exists
Nexo demonstrates a portfolio-ready baseline for user authentication with clear architecture boundaries, predictable API behavior, and a frontend flow that works end to end.

## Tech stack

### Backend
- Java 21
- Spring Boot 4.0.3
- Spring Web MVC + Validation
- Spring Data JPA
- Flyway
- PostgreSQL (local via Docker)
- H2 (tests)
- Maven Wrapper (`./mvnw`)

### Frontend
- React 19 + TypeScript
- Vite 6
- React Router DOM
- Vitest + Testing Library
- PWA manifest + service worker

## Getting started

### Prerequisites
- JDK 21
- Docker + Docker Compose
- Node.js + npm

### 1. Start PostgreSQL
```bash
docker compose -f docker/docker-compose.yml up -d postgres
```

### 2. Run backend
```bash
./mvnw spring-boot:run
```

Backend URL: `http://localhost:8080`

### 3. Run frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## Useful commands

### Backend
```bash
./mvnw -q test
./mvnw -q -DskipTests package
./mvnw -q -Dtest=ClassNameTest test
```

### Frontend
```bash
cd frontend
npm run dev
npm run test
npm run build
npm run lint
```

## Configuration

### Backend
Main files:
- `src/main/resources/application.properties`
- `src/main/resources/application-local.properties`

Common environment variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `APP_WEB_CORS_ALLOWED_ORIGINS`
- `APP_AUTH_JWT_SECRET`
- `APP_AUTH_JWT_ACCESS_TOKEN_TTL`
- `APP_AUTH_REFRESH_TOKEN_TTL`
- `APP_AUTH_REFRESH_TOKEN_COOKIE_NAME`
- `APP_AUTH_REFRESH_TOKEN_COOKIE_PATH`
- `APP_AUTH_REFRESH_TOKEN_COOKIE_SAME_SITE`
- `APP_AUTH_REFRESH_TOKEN_COOKIE_SECURE`
- `APP_AUTH_PASSWORD_RESET_TOKEN_TTL`
- `APP_AUTH_EMAIL_VERIFICATION_TOKEN_TTL`

Local-only seeded auth user:
- Active only when the `local` profile is active.
- Email: `local.dev@nexo.local`
- Password: `LocalDevOnly123!`
- Purpose: fast local login without manual DB edits.
- The seed is idempotent and will not create duplicates.

### Frontend
- `frontend/.env.example`
- `VITE_API_BASE_URL` (default local API: `http://localhost:8080`)

## API overview

Public endpoints:
- `GET /health`
- `POST /auth/login`
- `POST /auth/signup`
- `POST /auth/refresh`
- `POST /auth/logout`
- `POST /auth/logout-all`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`
- `POST /auth/verify-email?token=...`

Protected endpoints:
- `GET /me`
- `POST /auth/resend-verification`

Authentication behavior:
- Uses short-lived Bearer JWT access tokens (default: 15 minutes).
- Uses long-lived refresh tokens (default: 30 days) in HttpOnly cookies.
- `POST /auth/refresh` rotates refresh tokens (old token becomes invalid).
- `POST /auth/logout` revokes the current refresh session.
- `POST /auth/logout-all` revokes all refresh sessions for the refresh-token owner.
- Protected routes return `401` for missing/invalid access tokens, and `Access token expired` when JWT is expired.
- Invalid or compromised refresh tokens return `401` with `Invalid refresh token`.
- CORS preflight (`OPTIONS`) is allowed on protected routes.

## Project structure
```text
src/main/java/com/mariaribeiro/nexo
|-- NexoApplication.java
|-- adapters/in/rest/
|   `-- HealthController.java
|-- identity/
|   |-- domain/
|   |-- application/
|   |-- adapters/
|   |   |-- in/rest/
|   |   `-- out/
|   `-- infrastructure/config/
`-- infrastructure/web/

frontend/
|-- src/
|-- public/
`-- package.json
```

## Architecture (high level)
The backend follows DDD + hexagonal architecture:
- `domain`: core business model and rules
- `application`: use cases and ports
- `adapters/in`: REST controllers and request/response DTOs
- `adapters/out`: persistence, security, and delivery integrations
- `infrastructure`: Spring wiring and technical configuration

## Testing
- Backend tests: `./mvnw -q test`
- Frontend tests: `cd frontend && npm run test`

## CI
- Workflow: `.github/workflows/ci.yml`
- CI validates backend tests with PostgreSQL + Flyway migrations.

## Feature updates

### 2026-03-07 - Local development seeded auth user
- Added a development-only startup seeder for a default local login user when the `local` profile is active.
- Kept seeding idempotent by checking existing email before insert.
- Why it matters: developers can access auth-protected flows immediately without manual database edits.

### 2026-03-07 - Refresh token sessions and rotation
- Added cookie-based refresh token sessions with persistence, rotation, and revocation support.
- Added `POST /auth/refresh`, `POST /auth/logout`, and `POST /auth/logout-all`.
- Added clear auth failure semantics for expired access token vs invalid refresh token.
- Why it matters: users stay signed in over time without weakening basic session security.

### 2026-03-07 - DDD package refactor
- Consolidated backend packages around explicit hexagonal boundaries (`application`, `domain`, `adapters`, `infrastructure`).
- Kept endpoint behavior unchanged while improving maintainability and reviewability.
- Why it matters: the codebase is easier to navigate and safer to extend.

### 2026-03-06 - Auth flow foundation
- Added signup, login, email verification, forgot password, reset password, and protected identity endpoint (`/me`).
- Added consistent validation and generic error handling for auth-sensitive flows.
- Why it matters: the project demonstrates a complete authentication baseline.

### 2026-03-06 - Frontend auth UX foundation
- Added route guard, session validation, remember-me behavior, and auth flows for signup/login/reset/verify.
- Integrated API client and token handling with protected route navigation.
- Why it matters: reviewers can run a full frontend-backend auth flow locally.

## Notes for contributors
This repo uses agent guardrails in `AGENTS.md`. When feature work changes onboarding, setup, behavior, or usage, update `README.md` in the same change.
