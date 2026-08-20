import { useState } from 'react'
import { FiGithub, FiAlertCircle } from 'react-icons/fi'
import './RepositoryForm.css'

function RepositoryForm({ onRepositoryIndexed }) {
  const [url, setUrl] = useState('https://github.com/spring-projects/spring-boot')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setSuccess(null)

    if (!url.trim()) {
      setError('Please enter a GitHub URL')
      return
    }

    setLoading(true)

    try {
      const response = await fetch('/api/repos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ githubUrl: url })
      })

      const data = await response.json()

      if (data.success) {
        setSuccess(`Repository indexed successfully (ID: ${data.data.repoId})`)
        setUrl('')
        onRepositoryIndexed(data.data.repoId)
      } else {
        setError(data.error || 'Failed to index repository')
      }
    } catch (err) {
      setError('Error: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form className="repo-form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="url" className="form-label">
          <FiGithub className="icon" />
          GitHub Repository URL
        </label>
        <input
          id="url"
          type="text"
          placeholder="https://github.com/owner/repo"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          disabled={loading}
          className="form-input"
        />
        <small className="form-hint">
          Paste a GitHub repository URL to index it for querying
        </small>
      </div>

      <button
        type="submit"
        disabled={loading}
        className="btn btn-primary"
      >
        {loading ? (
          <>
            <span className="spinner-small"></span>
            Indexing...
          </>
        ) : (
          'Index Repository'
        )}
      </button>

      {error && (
        <div className="form-message error">
          <FiAlertCircle className="message-icon" />
          {error}
        </div>
      )}

      {success && (
        <div className="form-message success">
          ✅ {success}
        </div>
      )}
    </form>
  )
}

export default RepositoryForm
