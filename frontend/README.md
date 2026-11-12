# JobTrack frontend

Angular 22 single-page application (standalone components, signals, zoneless).

```bash
npm install
npm start          # http://localhost:4200, /api is proxied to http://localhost:8080
npm test           # unit tests (Vitest)
npm run build      # production build in dist/
```

## Structure

```
src/app/
├── core/           Cross-cutting code: session (AuthStore), JWT interceptor,
│                   route guards, API error mapping, form error messages, dates
├── auth/           Login and registration pages
└── applications/   Applications list, form, follow-ups, API client, model, validators
```

See [../docs/decisions.md](../docs/decisions.md) for the design choices.
