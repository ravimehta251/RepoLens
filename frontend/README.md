# RepoSage Frontend

Modern React frontend for RepoSage - AI-Powered GitHub Repository Q&A Engine.

## Features

✨ **Modern React UI**
- Built with React 18 and Vite for fast development
- Responsive design (mobile, tablet, desktop)
- Beautiful gradient design with smooth animations

🎯 **Core Functionality**
- Index GitHub repositories with a single URL
- Real-time status monitoring with auto-refresh
- Natural language Q&A interface
- Source code reference browser
- Auto-resizing text input
- Copy-to-clipboard functionality

🚀 **Developer Experience**
- Component-based architecture
- Zustand state management
- CSS modules and shared styles
- Fast HMR (Hot Module Replacement)
- Pre-configured API proxy

## Quick Start

### Prerequisites
- Node.js 16+ and npm/yarn/pnpm

### Installation

```bash
cd frontend
npm install
# or
yarn install
# or
pnpm install
```

### Development

```bash
npm run dev
```

Opens at `http://localhost:3000`

The frontend is configured to proxy API calls to `http://localhost:8080` (your Spring Boot backend).

### Build for Production

```bash
npm run build
```

Output in `dist/` folder, ready to serve.

## Project Structure

```
frontend/
├── src/
│   ├── components/           # React components
│   │   ├── RepositoryForm.jsx
│   │   ├── RepositoryStatus.jsx
│   │   ├── QueryInterface.jsx
│   │   ├── QueryResults.jsx
│   │   └── *.css            # Component styles
│   ├── store/
│   │   └── repoStore.js     # Zustand store (state management)
│   ├── App.jsx              # Main app component
│   ├── App.css              # App styles
│   ├── main.jsx             # Entry point
│   └── index.css            # Global styles
├── index.html               # HTML template
├── package.json
├── vite.config.js
├── .env.example
└── .gitignore
```

## Components

### RepositoryForm
- Input for GitHub repository URL
- Submits to `/api/repos` POST endpoint
- Shows success/error messages
- Loading state while indexing

### RepositoryStatus
- Displays repository metadata (owner, name)
- Shows current indexing status (PENDING, INDEXING, READY, FAILED)
- Displays chunk count
- Auto-refresh toggle for in-progress indexing
- Link to GitHub repository

### QueryInterface
- Text input for natural language questions
- Submit button with loading state
- Keyboard shortcut: Ctrl+Enter to submit
- Disabled when no repository is indexed

### QueryResults
- Displays AI-generated answer
- Collapsible source references with code snippets
- Copy-to-clipboard for code
- Shows file paths and line numbers

## State Management (Zustand)

```javascript
useRepoStore() provides:
- currentRepoId: ID of selected repository
- repoData: Current repository metadata
- repositories: List of indexed repositories
- setCurrentRepoId(): Set active repository
- setRepoData(): Update repository data
- clearCurrentRepo(): Clear selection
- addRepository(): Add to list
- updateRepository(): Update in list
```

## API Integration

The frontend communicates with the Spring Boot backend via REST:

```
POST /api/repos              - Index a repository
GET /api/repos/{id}          - Get repository status
POST /api/repos/{id}/query   - Ask a question
GET /api/repos/{id}/chunks   - Get debug chunk data
```

All API calls go through the Vite proxy configured in `vite.config.js`.

## Environment Variables

Create `.env` file:

```env
VITE_API_URL=http://localhost:8080
```

## Keyboard Shortcuts

| Key | Action |
|-----|--------|
| Ctrl+Enter | Submit question in QueryInterface |

## Styling

- **Global**: `src/index.css` - Reset, typography, scrollbars
- **App**: `src/App.css` - Layout, grid, animations
- **Components**: Individual `.css` files per component
- **Theme**: Purple gradient (#667eea → #764ba2)

### Color Palette

- Primary: #667eea (Purple)
- Secondary: #764ba2 (Dark Purple)
- Success: #28a745 (Green)
- Error: #dc3545 (Red)
- Warning: #ffc107 (Yellow)
- Info: #17a2b8 (Cyan)
- Text: #333
- Background: #f5f5f5

## Performance Optimizations

- Code splitting via Vite
- Lazy component loading
- Optimized re-renders with React hooks
- CSS animations use GPU acceleration
- Zustand for minimal state updates

## Browser Support

- Chrome/Chromium (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

### CORS Errors
Make sure Spring Boot backend is running on port 8080 and the API proxy in `vite.config.js` is configured correctly.

### Port Already in Use
Change port in `vite.config.js`:
```javascript
server: {
  port: 3001,  // Change to different port
}
```

### API Calls Not Working
1. Check backend is running: `curl http://localhost:8080/api/repos`
2. Check environment variables
3. Open browser DevTools → Network tab to see actual requests

## Development Tips

### Adding a New Component

```javascript
// components/MyComponent.jsx
import './MyComponent.css'

function MyComponent() {
  return <div className="my-component">Content</div>
}

export default MyComponent
```

```css
/* components/MyComponent.css */
.my-component {
  /* styles */
}
```

### Using the Store

```javascript
import { useRepoStore } from '../store/repoStore'

function MyComponent() {
  const { currentRepoId, setCurrentRepoId } = useRepoStore()
  
  return (
    <button onClick={() => setCurrentRepoId(123)}>
      Set Repo to 123
    </button>
  )
}
```

### Making API Calls

```javascript
const response = await fetch('/api/repos', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ githubUrl: url })
})
const data = await response.json()
```

## Next Steps

### Enhancement Ideas
- [ ] Query history sidebar
- [ ] Save favorite queries
- [ ] Multi-turn conversation
- [ ] Dark mode toggle
- [ ] Export results to PDF
- [ ] Share results link
- [ ] Repository search/filter
- [ ] Advanced settings modal
- [ ] Syntax highlighting for code
- [ ] Repository comparison

### Testing
Add Jest + React Testing Library:
```bash
npm install --save-dev @testing-library/react @testing-library/jest-dom jest
```

### Deployment
Build and serve with static host:
```bash
npm run build
# Upload dist/ folder to your hosting
```

## License

MIT - Part of RepoSage project
