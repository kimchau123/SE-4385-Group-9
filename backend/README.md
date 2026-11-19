# Skills Hub Backend

Spring Boot REST API for Skills Hub project and staffing-demand management. The module uses Spring Data JPA with PostgreSQL; schema updates are managed with Hibernate for the initial development phase.

## Configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to configure the PostgreSQL connection. Defaults target a local `skillshub` database with the local `postgres` credentials.

## Run and test

```bash
mvn spring-boot:run
mvn test
```

Project endpoints are rooted at `/api/projects`.
