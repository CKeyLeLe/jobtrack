# JobTrack

Web application for tracking job applications: companies, positions, status,
follow-up reminders and notes.

> Portfolio project, built step by step during the *Concepteur Développeur
> d'Applications* training (ENI).

## Stack

| Layer      | Technology                                             |
|------------|--------------------------------------------------------|
| Backend    | Java 21, Spring Boot 3.5, Maven, Spring Data JPA       |
| Security   | Spring Security, stateless JWT authentication          |
| Database   | PostgreSQL 17 (Docker), H2 for tests                   |
| Frontend   | Angular                                                |
| Tests      | JUnit 5, Mockito, MockMvc                              |
| DevOps     | Docker Compose, GitHub Actions                         |

## Roadmap

- [x] 1. Project skeleton: Spring Boot API, PostgreSQL via Docker Compose
- [ ] 2. Domain model: `User` and `JobApplication` entities, Flyway migrations
- [ ] 3. Applications CRUD: controller / service / repository layers, DTOs, validation
- [ ] 4. Error handling: centralized `@RestControllerAdvice`, RFC 7807 problem details
- [ ] 5. Status filter and follow-ups list
- [ ] 6. Security: registration, login, JWT, per-user data isolation
- [ ] 7. Unit tests (Mockito) and web tests (MockMvc)
- [ ] 8. Angular frontend
- [ ] 9. Full Docker Compose stack
- [ ] 10. CI with GitHub Actions

## Run locally

Requirements: JDK 21+, Docker. Maven does not need to be installed: the project
ships the Maven Wrapper (`mvnw`).

```bash
docker compose up -d          # start PostgreSQL
cd backend
./mvnw spring-boot:run        # start the API on http://localhost:8080
```

Health check: `http://localhost:8080/actuator/health`

Run the tests (no Docker needed, they use an in-memory H2 database):

```bash
cd backend
./mvnw test
```
