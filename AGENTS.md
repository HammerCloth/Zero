# Repository Guidelines

## Project Structure & Module Organization

The production application is split into `backend/` and `frontend-vue/`. The backend is a Spring Boot 3 service: Java code lives under `backend/src/main/java/com/zero/`, configuration and Flyway migrations under `backend/src/main/resources/`, and tests belong in `backend/src/test/java/`. The Vue 3 client keeps pages in `frontend-vue/src/views/`, reusable UI in `components/`, state in `stores/`, and HTTP clients in `api/`. Static assets are in `frontend-vue/public/` and `src/assets/`.

`frontend/` is the older React client; modify it only when a task explicitly targets that implementation. Deployment files are at the repository root (`docker-compose.yml`, `Caddyfile`), operational scripts are in `scripts/`, and behavior specifications are maintained in `openspec/specs/` with completed changes archived under `openspec/changes/archive/`.

## Build, Test, and Development Commands

- `cd backend && ./mvnw spring-boot:run` starts the API on port 8080.
- `cd backend && ./mvnw test` runs the Spring/JUnit test suite.
- `cd frontend-vue && npm ci && npm run dev` installs locked dependencies and starts Vite on port 5173.
- `cd frontend-vue && npm run build` type-checks and creates `frontend-vue/dist/`.
- `docker compose up -d --build` builds and starts the production-style stack.
- For legacy React work, use `cd frontend && npm test` and `npm run lint`.

## Coding Style & Naming Conventions

Use two-space indentation in Vue, TypeScript, YAML, and JSON; use the existing four-space Java continuation style and package namespace `com.zero`. Name Java types and Vue components in PascalCase (`DashboardService`, `SnapshotFormPage.vue`), functions and variables in camelCase, and database migrations `V<number>__<description>.sql`. Follow nearby code for quote and semicolon conventions. Keep controllers thin and place business rules in services.

## Testing Guidelines

Add JUnit tests under the matching backend package and name them `*Test.java`. The active Vue app currently has no automated test command, so `npm run build` is the minimum frontend check; manually verify affected routes and API states. Legacy Vitest files use `*.test.ts`. Add regression coverage for bug fixes when a suitable harness exists.

## Commit & Pull Request Guidelines

Recent history follows Conventional Commits: `feat(vue): ...`, `fix: ...`, and `chore(openspec): ...`. Write imperative, focused subjects and include a scope when useful. Pull requests should explain the user-visible change, list verification commands, link the relevant issue or OpenSpec change, and include screenshots for UI changes. Call out migrations, environment-variable changes, and deployment impact explicitly; never commit `.env`, database files, or secrets.
