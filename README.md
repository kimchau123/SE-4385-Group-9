# SE-4385-Group-9

Skills Hub is a cloud-based platform concept for tracking employee skills across organizational structures. It connects a standardized Skills Catalog, Organization Structure, and Skills Inventory to help show workforce capabilities across employees, roles, and teams.

## Run the frontend

Prerequisites: install [Node.js](https://nodejs.org/) with npm.

Open a terminal in the repository root and run:

```bash
cd frontend
npm ci
npm run dev
```

Open the local URL printed by Vite (usually `http://localhost:5173`). Stop the development server with `Ctrl+C`.

## Frontend commands

Run these commands from the `frontend` directory:

```bash
npm run build    # Type-check and create a production build in frontend/dist
npm run lint     # Run ESLint
npm run preview  # Preview the production build after running npm run build
```

The frontend currently uses illustrative sample data. A Spring Boot API is available in [`backend/`](backend/README.md).

## Run the backend

Prerequisites: Java 17+, Maven, and PostgreSQL. Create a PostgreSQL database named `skillshub`, then run:

```bash
cd backend
mvn spring-boot:run
```

By default the API connects to `jdbc:postgresql://localhost:5432/skillshub` with the `postgres` user and password. Override the connection with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Run backend tests with `mvn test` from `backend/`.
