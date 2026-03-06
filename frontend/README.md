# Frontend - Nexo PWA Foundation

## Overview
This frontend is a mobile-first React app prepared for PWA installability and environment-based API configuration.

## Delivered in this card
- Route placeholders:
  - `/login`
  - `/app`
- Centralized API access:
  - `src/api/httpClient.ts` as the single reusable HTTP entry point
  - `src/api/healthApi.ts` as an example endpoint module using the client
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
- Auth logic is intentionally out of scope for this phase.
- Final visual design is intentionally out of scope for this phase.
- Placeholder pages display the resolved API base URL so environment wiring can be verified in-browser.
- Future token injection should be added in `src/api/httpClient.ts`, where headers are built centrally.
