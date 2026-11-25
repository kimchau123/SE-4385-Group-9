## Requirements

- Node.js with npm

## Start the development server

From the repository root:

```bash
cd frontend
npm ci
npm run dev
```

Open the URL printed by Vite, usually `http://localhost:5173`. Press `Ctrl+C` in the terminal to stop the server.

## Other commands

Run these from the `frontend` directory:

```bash
npm run build    # Type-check and create a production build in dist/
npm run lint     # Run ESLint
npm run preview  # Preview the production build after building
```

## Source layout

- `src/App.tsx` — application shell, page state, navigation, and shared UI state
- `src/data.ts` — page types and sample data for the Inventory and Organization pages
- `src/api/` — data access; `catalog.ts` is the Skills Catalog API (real backend or built-in demo data)
- `src/components/` — reusable interface components and icons
- `src/pages/` — Home, Catalog, Add Skill, Inventory, Organization, and Reports pages
- `src/App.css` and `src/index.css` — application and global styles

## Skills Catalog data source

The Skills Catalog (Workforce Planner) runs on built-in demo data until the backend catalog endpoints exist. To call the Spring Boot backend instead, copy `.env.example` to `.env.local`, set `VITE_USE_MOCK_API=false`, start the backend on port 8080, and restart `npm run dev`. The dev server proxies `/api` to `http://localhost:8080`. Endpoint paths are listed at the top of `src/api/catalog.ts`.
