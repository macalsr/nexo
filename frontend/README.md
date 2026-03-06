# Frontend - Nexo PWA Foundation

## Overview
This frontend is a mobile-first React app prepared for PWA installability, environment-based API configuration, and MVP authentication entry.

## Delivered in this card
- Routes:
  - `/login` with validated email/password login form
  - `/app` protected by local access-token presence
- Centralized API access:
  - `src/api/httpClient.ts` as the single reusable HTTP entry point
  - `src/api/healthApi.ts` as an example endpoint module using the client
  - `src/api/authApi.ts` for `POST /auth/login`
- Session handling:
  - `src/auth/tokenStorage.ts` stores the access token in `localStorage` under `nexo.accessToken`
  - centralized `Authorization: Bearer <token>` header injection in `src/api/httpClient.ts`
- PWA basics:
  - `public/manifest.webmanifest`
  - `public/sw.js`
  - service worker registration in `src/main.tsx`
- Environment config:
  - `VITE_API_BASE_URL` consumed in `src/config/env.ts`
- Mobile-first shell:
  - route-based placeholder screens sized for small viewports first
  - basic installability metadata in `index.html`

## Commands
```bash
npm install
npm run dev
npm run build
```

## Environment
Copy and adapt:
```bash
cp .env.example .env
```

Variable:
- `VITE_API_BASE_URL` (example: `http://localhost:8080`)

## Notes
- Login validates required email/password fields and applies a basic email-format check before calling the API.
- Failed login attempts always surface the generic message `Invalid credentials`.
- The token is stored in `localStorage` for the MVP only; a more durable auth model can replace `src/auth/tokenStorage.ts` later.
- Final visual design remains intentionally out of scope for this phase.
