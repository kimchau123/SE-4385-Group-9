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

## Data operations services

The DataOps service accepts UTF-8 CSV bytes with exactly these headers:
`skillName,skillDescription,skillType`. Descriptions may be empty; skill types are
`KNOWLEDGE`, `EXPERIENCE`, `TECHNOLOGY`, or `OTHER`. Imported catalog skills start
in `REQUESTED` status. Inventory reports are generated as UTF-8 CSV.

Database backup and restore use the PostgreSQL `pg_dump` and `pg_restore` executables.
Configure their paths with `PG_DUMP_EXECUTABLE` and `PG_RESTORE_EXECUTABLE`; configure
the private archive directory with `DATAOPS_BACKUP_DIRECTORY` (default `./backups`).
Backups are tracked in the database and verified using SHA-256 before restore.

The data-operations service is implemented, but its HTTP controller is intentionally
not enabled yet: this application has no authentication provider, while CSV import,
report export, and especially database restore must be restricted to administrators.
Do not expose these operations until the team authentication layer enforces admin
authorization.
