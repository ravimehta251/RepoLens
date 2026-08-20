# React Frontend Implementation - Quick Start

## ✨ What's Been Created

A complete, production-ready React frontend for RepoSage with:

```
frontend/
├── 4 Reusable Components (RepositoryForm, Status, Query, Results)
├── Zustand State Management
├── Responsive Styling (Mobile/Tablet/Desktop)
├── 5 Dependencies (React, Zustand, react-icons, Vite)
├── Complete Documentation
└── Ready for npm install → npm run dev
```

## 🚀 Quick Start (5 minutes)

### 1. Open Terminal & Navigate
```bash
cd e:\Resume_Project\RepoLens\frontend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Start Dev Server
```bash
npm run dev
```

Output:
```
VITE ready on http://localhost:3000/
```

### 4. Open in Browser
Navigate to: **http://localhost:3000**

You should see:
- 🎨 Purple gradient header
- 📝 Repository indexing form (left panel)
- 📊 Status display area (appears after indexing)
- ❓ Question interface (appears when ready)

### 5. Test It Out

1. **Index a repo**:
   - Paste: `https://github.com/spring-projects/spring-boot`
   - Click: "Index Repository"
   - Wait: Status → READY (auto-refreshes every 3s)

2. **Ask a question**:
   - Type: "How does dependency injection work?"
   - Press: Ctrl+Enter
   - See: Answer + source code references

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/         ← Reusable UI components
│   │   ├── RepositoryForm.jsx & .css
│   │   ├── RepositoryStatus.jsx & .css
│   │   ├── QueryInterface.jsx & .css
│   │   └── QueryResults.jsx & .css
│   ├── store/
│   │   └── repoStore.js    ← Global state (Zustand)
│   ├── App.jsx             ← Main app layout
│   ├── main.jsx            ← React entry point
│   └── index.css           ← Global styles
├── index.html              ← HTML template
├── vite.config.js          ← Build config + API proxy
├── package.json            ← Dependencies
└── README.md               ← Frontend docs
```

## 🎯 Key Features

| Feature | Component | Details |
|---------|-----------|---------|
| **Index Repos** | RepositoryForm | Paste GitHub URL → Index in background |
| **Monitor Status** | RepositoryStatus | Real-time status (PENDING/INDEXING/READY) |
| **Ask Questions** | QueryInterface | Natural language input → Ctrl+Enter |
| **View Results** | QueryResults | Answer + Collapsible source code |
| **State Management** | repoStore (Zustand) | Global state for repo data |

## 🔌 Backend Integration

The React app automatically proxies API calls to your Spring Boot backend:

```
React (http://localhost:3000)
  └── Vite Proxy (vite.config.js)
    └── Spring Boot (http://localhost:8080)
      └── PostgreSQL + pgvector
```

**No manual CORS configuration needed** - Vite handles it!

## 📦 Dependencies

```json
{
  "react": "^18.2.0",              // UI framework
  "react-dom": "^18.2.0",          // DOM rendering
  "zustand": "^4.4.0",             // State management
  "react-icons": "^4.12.0",        // Icon library
  "axios": "^1.6.0"                // HTTP client (optional)
}
```

## 🎨 Styling Features

- **Gradient theme**: Purple (#667eea) → Dark Purple (#764ba2)
- **Responsive grid**: Auto-adapts to mobile/tablet/desktop
- **Smooth animations**: Fade-in, slide-in, spinner effects
- **Color palette**: 6 semantic colors (success, error, info, warning)
- **CSS modules**: Component-scoped + global styles

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| **Ctrl+Enter** | Submit question in QueryInterface |
| **Tab** | Navigate between fields |
| **Click source** | Expand/collapse code snippet |

## 🔍 Component Details

### RepositoryForm
- Input GitHub URL
- Validate format
- Show loading state
- Display success/error messages
- Auto-populated with example

### RepositoryStatus
- Show owner/name/status badge
- Display chunk count
- Auto-refresh toggle
- Manual refresh button
- Link to GitHub repository

### QueryInterface
- Auto-resizing textarea
- Keyboard shortcuts
- Disabled when no repo indexed
- Loading state during query

### QueryResults
- Full answer text
- Collapsible source references
- Code syntax (dark theme)
- Copy-to-clipboard button
- File paths + line numbers

## 💾 State Management (Zustand)

```javascript
// Use in any component
import { useRepoStore } from './store/repoStore'

function MyComponent() {
  const { currentRepoId, repoData, setCurrentRepoId } = useRepoStore()
  
  return (
    <button onClick={() => setCurrentRepoId(123)}>
      Set Repo to 123
    </button>
  )
}
```

**Available actions**:
- `setCurrentRepoId(id)` - Select a repo
- `setRepoData(data)` - Update repo info
- `clearCurrentRepo()` - Clear selection
- `addRepository(repo)` - Add to list
- `updateRepository(id, updates)` - Update in list

## 🧪 Testing Components

### In Browser DevTools

```javascript
// Check API calls
fetch('/api/repos').then(r => r.json()).then(console.log)

// Access store state
import { useRepoStore } from './store/repoStore'
console.log(useRepoStore.getState())

// Check localStorage
localStorage.getItem('repoSageRepoId')
```

### With React DevTools Extension

Install: https://react-devtools-tutorial.vercel.app/

Then inspect:
- Component tree
- Props & state
- Re-render performance

## 📱 Responsive Breakpoints

```css
Desktop:        > 1024px   (2-column grid)
Tablet:         600-1024px (2-column grid)
Mobile:         < 600px    (1-column stack)
```

Test responsiveness:
1. Open DevTools (F12)
2. Click "Toggle device toolbar" (Ctrl+Shift+M)
3. Select device or custom dimensions

## 🛠️ Common Development Tasks

### Add a New Component

```bash
# Create component file
touch src/components/MyFeature.jsx
touch src/components/MyFeature.css
```

```jsx
// src/components/MyFeature.jsx
import './MyFeature.css'

function MyFeature() {
  return <div className="my-feature">Content</div>
}

export default MyFeature
```

### Use the API

```javascript
// POST to index repository
const response = await fetch('/api/repos', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ githubUrl: url })
})
const { success, data, error } = await response.json()

if (success) {
  console.log('Indexed repo:', data.repoId)
}
```

### Add a Global Style

Edit `src/index.css` for changes that affect all components.

## 🐛 Debugging Tips

### Issue: Components not re-rendering
**Solution**: Check that you're using hooks properly (useState, useEffect, etc.)

### Issue: API calls return 404
**Solution**: Ensure backend is running on port 8080
```bash
docker-compose up
```

### Issue: Styles not applying
**Solution**: Make sure CSS file is imported
```javascript
import './ComponentName.css'  // Must import!
```

### Issue: Port 3000 already in use
**Solution**: Change port in `vite.config.js`
```javascript
server: { port: 3001 }
```

## 📚 Learn More

- **React**: https://react.dev/
- **Vite**: https://vitejs.dev/
- **Zustand**: https://github.com/pmndrs/zustand
- **react-icons**: https://react-icons.github.io/react-icons/

## 🚀 Build for Production

```bash
npm run build
```

Creates optimized `dist/` folder (~150KB gzipped).

### Serve locally for testing
```bash
npm run preview
```

### Deploy to production
- **Option 1**: Copy `dist/` → Backend's `static/` folder
- **Option 2**: Deploy to Netlify, Vercel, GitHub Pages, AWS S3, etc.

## ✅ Checklist

- [ ] Backend running on http://localhost:8080
- [ ] Ran `npm install` in frontend folder
- [ ] Started dev server: `npm run dev`
- [ ] Opened http://localhost:3000 in browser
- [ ] Tested indexing a repo
- [ ] Tested asking a question
- [ ] Opened DevTools to see API calls

## 🎉 You're All Set!

Your React frontend is ready to use. Start developing with:

```bash
npm run dev
```

Then explore, customize, and extend!

---

**Frontend Status**: ✅ Complete and ready for development
**Total Components**: 4 (RepositoryForm, Status, Query, Results)
**Total Files**: 11 (JSX + CSS)
**Build Tool**: Vite 5
**Package Manager**: npm/yarn/pnpm
