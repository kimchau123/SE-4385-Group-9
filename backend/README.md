# Skills Hub Backend

Spring Boot REST API for Skills Hub project and staffing-demand management. The module uses Spring Data JPA with PostgreSQL; schema updates are managed with Hibernate for the initial development phase.

## Configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to configure the PostgreSQL connection. Defaults target a local `skillshub` database with the local `postgres` credentials.

## Run and test

```bash
mvn spring-boot:run
mvn test
```

## Skills catalog endpoints

- `GET /api/skills` — list skills. Optional filters, in any combination: `query` (full or partial name, case-insensitive), `type` (`KNOWLEDGE`, `EXPERIENCE`, `TECHNOLOGY`, `OTHER`), `status` (`REQUESTED`, `APPROVED`, `CANCELED`). An unknown `type` or `status` returns 400.
- `GET /api/skills/{id}` — one skill.
- `POST /api/skills` — add a skill (`skillName` 1–50 characters, optional `skillDescription` 10–500 characters, `skillType`). New skills are always `REQUESTED`; a name that already exists (ignoring case) returns 409.
- `PUT /api/skills/{id}` — change a skill's name, description and type. Its status is unchanged.
- `DELETE /api/skills/{id}` — delete a skill. Returns 409 if any person has the skill or any project requires it; deny it instead.
- `PUT /api/skills/{id}/approve` and `PUT /api/skills/{id}/deny` — admin only. Moves a `REQUESTED` skill to `APPROVED` or `CANCELED`; any other status returns 409.
- `GET /api/skills/proficiency-levels` — the five proficiency levels, in order (1 = Beginner … 5 = Expert).
- `PUT /api/skills/proficiency-levels/{id}` — rename a level (`skillProficiencyLevelName`); its id stays the same. Levels can't be added, because Inventory and Projects use the fixed 1–5 scale.

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

## Authentication

Every `/api/**` request needs HTTP Basic credentials: the **person id** as the username and the shared demo password (`demo123` by default, set with `AUTH_DEMO_PASSWORD`). `GET /api/auth/me` returns the logged-in user (`personId`, `personName`, `role`, `isAdmin`).

Roles are informational. Anyone logged in can read everything. Admin-only actions (`isAdmin = 1`) return 403 for other users: creating or changing projects and project skills, approving or canceling person skills and project assignments, approving or denying catalog skills, and `/api/dataops/**`.

Demo accounts (after loading the seed):

| Person id | Name | Role |
|---|---|---|
| 987 | Nicole McLean | Business Operations (admin) |
| 988 | Crosby Reeves | Workforce Planner |
| 110 | Mylo Hobbs | Organizational Leader |
| 8 | Brantley Whitney | Resource Manager |
| 1 | Kamryn Villa | Employee |

```bash
curl -u 987:demo123 http://localhost:8080/api/auth/me
```

## Load sample data

The seed file loads the sponsor's sample data (organizations, people, skills, projects). It doesn't create tables, Hibernate creates them from the entities.

1. Start the backend once (`mvn spring-boot:run`) so the tables and the five proficiency levels are created, then stop it.
2. From `backend/`:

```bash
psql -U postgres -d skillshub -v ON_ERROR_STOP=1 -f skillshub_seed.sql
```

Safe to re-run: it clears existing data first (proficiency levels are kept).


## Data operations services

The DataOps service accepts UTF-8 CSV bytes with exactly these headers:
`skillName,skillDescription,skillType`. Descriptions may be empty; skill types are
`KNOWLEDGE`, `EXPERIENCE`, `TECHNOLOGY`, or `OTHER`. Imported catalog skills start
in `REQUESTED` status. Inventory reports are generated as UTF-8 CSV.

Database backup and restore use the PostgreSQL `pg_dump` and `pg_restore` executables.
Configure their paths with `PG_DUMP_EXECUTABLE` and `PG_RESTORE_EXECUTABLE`; configure
the private archive directory with `DATAOPS_BACKUP_DIRECTORY` (default `./backups`).
Backups are tracked in the database and verified using SHA-256 before restore.

Data operations are exposed at `/api/dataops` and require `ROLE_ADMIN`:

- `POST /api/dataops/validate` — validate an uploaded CSV.
- `POST /api/dataops/import` — import valid skills with `REQUESTED` status.
- `GET /api/dataops/imports/{importId}` — retrieve import history.
- `GET /api/dataops/report?orgId={id}` — download an organization inventory CSV.
- `POST /api/dataops/backups` — create a PostgreSQL backup.
- `POST /api/dataops/backups/{backupId}/restore` — restore a verified backup.
