# Architecture decisions

Short record of the main technical choices and their trade-offs.

## 1. Project setup

- **Monorepo** (`backend/`, later `frontend/`): one history, one CI pipeline.
- **Spring Boot 3.5 on Java 21**: widely used LTS stack. Spring Boot 4 exists;
  3.5 was chosen deliberately as the version most teams run today.
- **Configuration through environment variables** with local defaults
  (`${DB_URL:...}`): the same artifact runs locally, in Docker and in production.
- **Tests on H2 (PostgreSQL mode)**: fast and Docker-free. Trade-off: H2 is not
  PostgreSQL, so the schema is also verified against a real PostgreSQL.

## 2. Domain model and schema

- **Flyway owns the schema**, Hibernate only validates it (`ddl-auto=validate`).
  Migrations are versioned, reviewed in Git and replayed identically everywhere.
  A migration is never edited once applied: changes go into a new `V<n>__*.sql`.
- **Package by feature** (`user`, `jobapplication`) rather than by layer: each
  feature keeps its entity, repository, service and controller together.
- **Unidirectional `@ManyToOne` from `JobApplication` to `User`**, `LAZY`.
  `User` has no collection of applications: they are always fetched by query
  (filtered, sorted) and never loaded by accident.
- **Ownership in every query** (`findByIdAndUserId`): a user can never reach
  another user's application, even by guessing an id.
- **Enum stored as text** (`EnumType.STRING`) plus a database `CHECK`
  constraint: readable data, safe reordering, invalid values rejected twice.
- **Emails normalized to lowercase** in the entity so the unique constraint is
  case-insensitive in practice.
- **Entities never leave the service layer**: controllers will use DTOs
  (step 3) to control the API contract and never expose `passwordHash`.
- **No Lombok**: explicit constructors and getters; entities have no public
  no-arg constructor, so required fields cannot be forgotten.
