# Frontend - Nexo PWA Foundation

## Overview
This frontend is a mobile-first React app prepared for PWA installability, environment-based API configuration, and MVP authentication entry.

## Delivered in this card
- Routes:
  - `/login` with validated email/password login form
  - `/forgot-password` with generic reset-request success handling
  - `/signup` with validated account creation and immediate authenticated access on success
  - `/app` protected by centralized session validation
- Centralized API access:
  - `src/api/httpClient.ts` as the single reusable HTTP entry point
  - `src/api/healthApi.ts` as an example endpoint module using the client
  - `src/api/authApi.ts` for `POST /auth/login`
  - `src/api/authApi.ts` for `POST /auth/forgot-password`
  - `src/api/authApi.ts` for `POST /auth/signup`
- Session handling:
  - `src/auth/tokenStorage.ts` stores the access token in `localStorage` under `nexo.accessToken`
  - centralized `Authorization: Bearer <token>` header injection in `src/api/httpClient.ts`
  - `src/routing/ProtectedRoute.tsx` validates stored sessions with `GET /me` before rendering `/app`
  - `src/pages/AppPage.tsx` exposes a `Logout` action that clears the stored token and returns to `/login`
  - successful signup also stores the returned access token and enters the protected app shell immediately
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
npm test
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
- Forgot-password requests validate email format on the client and route to `POST /auth/forgot-password`.
- Signup validates required email/password fields, enforces a minimum password length of `8`, and uses Option B automatic authentication on success.
- Email normalization is explicit and consistent with the backend: accepted emails are trimmed before submission, and the backend persists them in lowercase.
- Failed login attempts always surface the generic message `Invalid credentials`.
- Duplicate signup attempts surface the generic message `Unable to create account`.
- Forgot-password success is intentionally generic (`Check your email`) so the UI does not reveal whether the account exists.
- The token is stored in `localStorage` for the MVP only; a more durable auth model can replace `src/auth/tokenStorage.ts` later.
- Password reset delivery is currently stubbed on the backend, so the request flow persists an expiring token but does not call a real email provider yet.
- Protected content is never rendered until `src/routing/ProtectedRoute.tsx` confirms the stored token with `GET /me`.
- If `GET /me` returns `401`, the token is cleared and the user is redirected back to `/login`.
- The `/login` and `/signup` screens remain reachable even if a stale token is still stored locally; only the protected route decides whether the session is valid.
- Signing out from `/app` clears `nexo.accessToken` before navigating back to `/login`.
- Automated route-guard coverage lives in `src/routing/ProtectedRoute.test.tsx` and runs with Vitest in `jsdom`.
- Automated forgot-password coverage lives in `src/pages/ForgotPasswordPage.test.tsx`.
- Automated signup coverage lives in `src/pages/SignupPage.test.tsx`.
- Final visual design remains intentionally out of scope for this phase.
