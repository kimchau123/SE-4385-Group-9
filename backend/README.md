# Skills Hub Backend

Spring Boot REST API for Skills Hub project and staffing-demand management. The module uses Spring Data JPA with PostgreSQL; schema updates are managed with Hibernate for the initial development phase.

## Configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to configure the PostgreSQL connection. Defaults target a local `skillshub` database with the local `postgres` credentials.

## Run and test

```bash
mvn spring-boot:run
mvn test
```

## Skills inventory endpoints

- `GET /api/inventory/persons/{personId}` — list a person's skills.
- `POST /api/inventory/persons/{personId}/skills` — add a skill request (`skillId`, `levelId`).
- `PUT /api/inventory/proficiency` — update proficiency (`personId`, `skillId`, `levelId`); the level must be 1–5 and the request returns to `REQUESTED`.
- `PUT /api/inventory/proficiency/status` — approve or cancel a pending skill request (`personId`, `skillId`, `status`).
- `GET /api/inventory/skills/{skillId}` — list people associated with a skill.
- `POST /api/inventory/assignments` — request a project assignment (`personId`, `projectId`).
- `PUT /api/inventory/assignments/status` — approve or cancel a pending assignment (`personId`, `projectId`, `status`).
- `GET /api/inventory/persons/{personId}/assignments` and `GET /api/inventory/projects/{projectId}/assignments` — list assignments.

Project endpoints are rooted at `/api/projects`.

## Load sample data

The seed file loads the sponsor's sample data (organizations, people, skills, projects). It doesn't create tables, Hibernate creates them from the entities.

1. Start the backend once (`mvn spring-boot:run`) so the tables and the five proficiency levels are created, then stop it.
2. From `backend/`:

```bash
psql -U postgres -d skillshub -v ON_ERROR_STOP=1 -f skillshub_seed.sql
```

Safe to re-run: it clears existing data first (proficiency levels are kept).

