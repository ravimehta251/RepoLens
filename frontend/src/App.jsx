import { useState, useEffect } from 'react'
import RepositoryForm from './components/RepositoryForm'
import RepositoryStatus from './components/RepositoryStatus'
import QueryInterface from './components/QueryInterface'
import QueryResults from './components/QueryResults'
import { useRepoStore } from './store/repoStore'
import './App.css'

function App() {
  const { currentRepoId, repoData, setRepoData, setCurrentRepoId } = useRepoStore()
  const [results, setResults] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Load repo from localStorage on mount
  useEffect(() => {
    const savedRepoId = localStorage.getItem('repoSageRepoId')
    if (savedRepoId) {
      setCurrentRepoId(parseInt(savedRepoId))
      fetchRepoStatus(parseInt(savedRepoId))
    }
  }, [])

  const fetchRepoStatus = async (repoId) => {
    try {
      const response = await fetch(`/api/repos/${repoId}`)
      const data = await response.json()
      if (data.success) {
        setRepoData(data.data)
      }
    } catch (err) {
      console.error('Error fetching repo status:', err)
    }
  }

  const handleRepositoryIndexed = (repoId) => {
    localStorage.setItem('repoSageRepoId', repoId)
    setCurrentRepoId(repoId)
    setResults(null)
    setError(null)
  }

  const handleQuerySubmit = async (question) => {
    if (!currentRepoId) {
      setError('No repository indexed')
      return
    }

    setLoading(true)
    setError(null)
    setResults(null)

    try {
      const response = await fetch(`/api/repos/${currentRepoId}/query`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question })
      })

      const data = await response.json()

      if (data.success) {
        setResults(data.data)
      } else {
        setError(data.error || 'Failed to process query')
      }
    } catch (err) {
      setError('Error: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-content">
          <h1>🔍 RepoSage</h1>
          <p>Ask questions about any GitHub repository using AI</p>
        </div>
      </header>

      <main className="app-main">
        <div className="app-container">
          <div className="left-panel">
            <section className="card">
              <h2>Repository Management</h2>
              <RepositoryForm onRepositoryIndexed={handleRepositoryIndexed} />
            </section>

            {currentRepoId && repoData && (
              <section className="card">
                <h2>Repository Status</h2>
                <RepositoryStatus 
                  repo={repoData}
                  onStatusUpdate={() => fetchRepoStatus(currentRepoId)}
                />
              </section>
            )}
          </div>

          <div className="right-panel">
            {currentRepoId && repoData?.status === 'READY' && (
              <section className="card">
                <h2>Ask Question</h2>
                <QueryInterface 
                  onSubmit={handleQuerySubmit}
                  loading={loading}
                />
              </section>
            )}

            {error && (
              <section className="card error-card">
                <p className="error-message">❌ {error}</p>
              </section>
            )}

            {results && (
              <section className="card">
                <h2>Answer</h2>
                <QueryResults results={results} />
              </section>
            )}

            {loading && (
              <section className="card">
                <div className="loading-spinner">
                  <div className="spinner"></div>
                  <p>Processing your question...</p>
                </div>
              </section>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}

export default App
