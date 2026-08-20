# React Frontend Setup Guide

## Overview

The RepoSage frontend is a modern React application built with Vite. It provides a responsive, interactive UI for indexing GitHub repositories and querying them using AI.

## System Requirements

- **Node.js**: 16.x or higher
- **npm**: 7.x or higher (or yarn/pnpm)
- **Backend**: Spring Boot running on `http://localhost:8080`
- **OS**: Windows, macOS, or Linux

## Installation & Setup

### Step 1: Navigate to Frontend Directory

```bash
cd e:\Resume_Project\RepoLens\frontend
```

### Step 2: Install Dependencies

```bash
npm install
```

Or with yarn:
```bash
yarn install
```

Or with pnpm:
```bash
pnpm install
```

This installs:
- `react` & `react-dom` - UI framework
- `axios` - HTTP client (optional, currently using fetch)
- `zustand` - State management
- `react-icons` - Icon library
- `vite` & plugins - Build tool

### Step 3: Verify Backend is Running

Make sure the Spring Boot backend is running:

```bash
# In a separate terminal, from project root
docker-compose up
# or
mvn spring-boot:run
```

Backend should be accessible at: `http://localhost:8080`

### Step 4: Start Development Server

```bash
npm run dev
```

Output will show:
```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:3000/
  ➜  press h + enter to show help
```

### Step 5: Open in Browser

Navigate to: `http://localhost:3000`

You should see the RepoSage interface with:
- Purple gradient header
- Repository management form
- (Empty) query interface (until repo is indexed)

## First Usage

### 1. Index a Repository

1. In the form, paste a GitHub URL:
   ```
   https://github.com/spring-projects/spring-boot
   ```

2. Click "Index Repository"

3. Wait for status to show "READY" (auto-refreshes)
   - Initially shows: PENDING
   - Then: INDEXING
   - Finally: READY (with chunk count)

### 2. Ask a Question

1. Once status is READY, the question interface appears below

2. Type a question:
   ```
   How does Spring Boot handle dependency injection?
   ```

3. Press Ctrl+Enter or click "Ask Question"

4. Wait for answer (usually 2-10 seconds)

5. Click source references to expand code snippets

## File Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── RepositoryForm.jsx          # Index form component
│   │   ├── RepositoryForm.css
│   │   ├── RepositoryStatus.jsx        # Status display & refresh
│   │   ├── RepositoryStatus.css
│   │   ├── QueryInterface.jsx          # Question input
│   │   ├── QueryInterface.css
│   │   ├── QueryResults.jsx            # Answer & sources display
│   │   └── QueryResults.css
│   ├── store/
│   │   └── repoStore.js               # Zustand state store
│   ├── App.jsx                         # Main app layout
│   ├── App.css
│   ├── main.jsx                        # React entry point
│   ├── index.css                       # Global styles
│   └── utils/                          # (Future: API helpers, etc.)
├── index.html                          # HTML template
├── vite.config.js                      # Build configuration
├── package.json                        # Dependencies
├── .env.example                        # Environment variables template
├── .gitignore
└── README.md                           # Frontend docs
```

## Components Overview

### RepositoryForm
- **Purpose**: Index new repositories
- **Endpoint**: `POST /api/repos`
- **Features**:
  - GitHub URL input with validation
  - Loading state during indexing
  - Success/error messages
  - Pre-filled with example URL

### RepositoryStatus
- **Purpose**: Display and monitor repository status
- **Endpoint**: `GET /api/repos/{id}`
- **Features**:
  - Current status badge (PENDING, INDEXING, READY, FAILED)
  - Chunk count display
  - Auto-refresh toggle
  - Link to GitHub repository
  - Manual refresh button

### QueryInterface
- **Purpose**: Accept natural language questions
- **Endpoint**: `POST /api/repos/{id}/query`
- **Features**:
  - Auto-resizing textarea
  - Keyboard shortcuts (Ctrl+Enter)
  - Disabled when repo not ready
  - Loading state during query

### QueryResults
- **Purpose**: Display AI-generated answers
- **Features**:
  - Full answer text
  - Collapsible source references
  - Code snippet viewer
  - Copy-to-clipboard
  - File paths and line numbers

## State Management

Using **Zustand** for global state:

```javascript
// Access store
const { currentRepoId, repoData, setCurrentRepoId } = useRepoStore()

// Available actions:
store.setCurrentRepoId(123)
store.setRepoData(repoObject)
store.clearCurrentRepo()
store.addRepository(repo)
store.updateRepository(id, updates)
```

## API Integration

### Available Endpoints

| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/repos` | `{githubUrl: string}` | `{success: bool, data: Repo, error: string}` |
| GET | `/api/repos/{id}` | - | `{success: bool, data: Repo}` |
| POST | `/api/repos/{id}/query` | `{question: string}` | `{success: bool, data: QueryResponse}` |
| GET | `/api/repos/{id}/chunks` | - | `{success: bool, data: [chunks]}` |

### Example API Calls

```javascript
// Index repository
const res = await fetch('/api/repos', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ githubUrl: 'https://github.com/owner/repo' })
})
const data = await res.json()
// Returns: { success: true, data: { repoId: 1, owner, name, status, chunkCount } }

// Check status
const res = await fetch('/api/repos/1')
const data = await res.json()
// Returns: { success: true, data: { repoId, owner, name, status, chunkCount } }

// Ask question
const res = await fetch('/api/repos/1/query', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ question: 'How does X work?' })
})
const data = await res.json()
// Returns: { success: true, data: { answer: string, sourceReferences: [...] } }
```

## Development Tips

### Hot Module Replacement (HMR)

Changes to `.jsx` and `.css` files auto-refresh instantly in the browser without losing state.

### Environment Variables

Create `.env` file:
```env
VITE_API_URL=http://localhost:8080
```

Access in code:
```javascript
const apiUrl = import.meta.env.VITE_API_URL
```

### Debugging

1. **Browser DevTools** (F12):
   - Elements tab: Inspect DOM
   - Console tab: View logs
   - Network tab: Monitor API calls
   - Application tab: Check localStorage

2. **React DevTools Extension**:
   - Install: https://react-devtools-tutorial.vercel.app/
   - Inspect component tree
   - Check props and state

3. **Zustand DevTools**:
   Add debug middleware:
   ```javascript
   // In store/repoStore.js
   import { devtools } from 'zustand/middleware'
   ```

### Adding a New Feature

**Example: Add a favorites list**

1. Create component: `src/components/FavoritesList.jsx`
2. Add to store: `src/store/repoStore.js`
   ```javascript
   favorites: [],
   addFavorite: (repo) => set((state) => ({
     favorites: [...state.favorites, repo]
   }))
   ```
3. Import and use in `src/App.jsx`

## Production Build

```bash
npm run build
```

Creates optimized `dist/` folder (~150KB gzipped):

```bash
# Serve locally for testing
npm run preview
```

### Deployment Options

#### Option 1: Serve with Backend
Copy `dist/` contents to `src/main/resources/static/` in Java project:
```bash
cp -r dist/* ../src/main/resources/static/
```

Build backend, frontend loads at `http://localhost:8080`

#### Option 2: Separate Frontend Hosting
Deploy `dist/` to:
- **Netlify**: Drag & drop deployment
- **Vercel**: Connected to git repo
- **GitHub Pages**: Free static hosting
- **AWS S3 + CloudFront**: Production CDN
- **Nginx/Apache**: Self-hosted

### CORS Configuration

If frontend and backend are on different domains, ensure backend allows CORS:

```java
// In RepoSageApplication.java or SecurityConfig.java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*");
            }
        };
    }
}
```

## Troubleshooting

### Issue: "Cannot GET /" when opening localhost:3000

**Solution**: Make sure Vite dev server is running:
```bash
npm run dev
```

### Issue: API calls failing with 404

**Solution**: Check backend is running:
```bash
curl http://localhost:8080/api/repos
```

If not, start backend:
```bash
docker-compose up
```

### Issue: Port 3000 already in use

**Solution**: Change port in `vite.config.js`:
```javascript
server: {
  port: 3001,  // Change this
}
```

### Issue: Styling looks different on mobile

**Solution**: Check browser zoom is at 100% and viewport is correct in DevTools

### Issue: TypeScript errors (if using .ts files)

Add `tsconfig.json`:
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "jsx": "react-jsx",
    "module": "ESNext",
    "moduleResolution": "bundler"
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.app.json" }]
}
```

## Performance Monitoring

### Vite Build Analysis

```bash
npm install -g rollup-plugin-visualizer
# Then check build size in dist/
```

### Frontend Metrics

In browser DevTools Performance tab:
1. Open DevTools → Performance
2. Click record
3. Perform actions (index repo, query)
4. Click stop
5. Analyze flame chart

Current performance targets:
- Page load: < 2 seconds
- Query response: 2-10 seconds (depends on backend)
- Animations: 60 FPS

## Browser DevTools Tips

### Network Tab
- Monitor API calls
- Check response times
- Verify Content-Type headers

### Console Tab
- View JavaScript errors
- Run custom commands
- Check API responses:
  ```javascript
  // Manually test API
  fetch('/api/repos').then(r => r.json()).then(console.log)
  ```

### Application Tab
- View localStorage data (repo ID)
- Check cookies
- View cached data

## Next Steps

### Add Features

**Query History Sidebar**
```jsx
// Show recent queries
// Click to re-run
// Save favorites
```

**Dark Mode**
```jsx
// Theme provider
// Toggle in header
// Persist preference
```

**Code Syntax Highlighting**
```bash
npm install highlight.js
```

**Export Results**
```jsx
// Export to PDF
// Export to markdown
// Share link
```

### Integration with Backend

Ensure backend exposes all required endpoints in `API.md`:
- ✅ `POST /api/repos` - Index
- ✅ `GET /api/repos/{id}` - Status
- ✅ `POST /api/repos/{id}/query` - Query
- ✅ `GET /api/repos/{id}/chunks` - Debug

### Deployment Checklist

- [ ] Build passes: `npm run build`
- [ ] No console errors in production build
- [ ] Test on mobile devices
- [ ] Test in multiple browsers
- [ ] Update API URLs for production
- [ ] Set up monitoring/analytics
- [ ] Configure CORS if needed
- [ ] Set up SSL/HTTPS
- [ ] Enable caching headers

## Support & Documentation

- **Frontend README**: [frontend/README.md](./README.md)
- **Backend API**: [../API.md](../API.md)
- **Vite Docs**: https://vitejs.dev/
- **React Docs**: https://react.dev/
- **Zustand Docs**: https://github.com/pmndrs/zustand

---

**Status**: ✅ Complete React frontend ready for development
