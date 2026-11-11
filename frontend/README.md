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
- `src/data.ts` — page, skill, and sample data types/constants
- `src/components/` — reusable interface components and icons
- `src/pages/` — Home, Catalog, Add Skill, Inventory, Organization, and Reports pages
- `src/App.css` and `src/index.css` — application and global styles
