import { useEffect, useState } from 'react'
import { FiRotateCw } from 'react-icons/fi'
import './RepositoryStatus.css'

function RepositoryStatus({ repo, onStatusUpdate }) {
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [autoRefresh, setAutoRefresh] = useState(repo?.status !== 'READY')

  useEffect(() => {
    if (!autoRefresh || repo?.status === 'READY' || repo?.status === 'FAILED') {
      return
    }

    const timer = setTimeout(() => {
      handleRefresh()
    }, 3000)

    return () => clearTimeout(timer)
  }, [autoRefresh, repo?.status])

  const handleRefresh = async () => {
    setIsRefreshing(true)
    await onStatusUpdate()
    setIsRefreshing(false)
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'READY':
        return 'status-ready'
      case 'INDEXING':
        return 'status-indexing'
      case 'PENDING':
        return 'status-pending'
      case 'FAILED':
        return 'status-failed'
      default:
        return 'status-pending'
    }
  }

  const getStatusIcon = (status) => {
    switch (status) {
      case 'READY':
        return '✅'
      case 'INDEXING':
        return '⏳'
      case 'PENDING':
        return '⏱️'
      case 'FAILED':
        return '❌'
      default:
        return '❓'
    }
  }

  return (
    <div className="repo-status">
      <div className="status-header">
        <div className="repo-title">
          <h3>
            {repo?.owner}/{repo?.name}
          </h3>
          <a 
            href={`https://github.com/${repo?.owner}/${repo?.name}`}
            target="_blank"
            rel="noopener noreferrer"
            className="repo-link"
          >
            View on GitHub →
          </a>
        </div>

        <button 
          className="btn-refresh"
          onClick={handleRefresh}
          disabled={isRefreshing}
          title="Refresh status"
        >
          <FiRotateCw className={isRefreshing ? 'spinning' : ''} />
        </button>
      </div>

      <div className="status-row">
        <label>Status</label>
        <div className={`status-badge ${getStatusColor(repo?.status)}`}>
          <span className="status-icon">{getStatusIcon(repo?.status)}</span>
          <span className="status-text">{repo?.status}</span>
        </div>
      </div>

      <div className="status-row">
        <label>Chunks Indexed</label>
        <div className="chunk-count">
          {repo?.chunkCount || 0} chunks
        </div>
      </div>

      {repo?.status === 'READY' && (
        <div className="status-message success">
          ✅ Repository is ready for queries
        </div>
      )}

      {repo?.status === 'INDEXING' && (
        <div className="status-message info">
          ⏳ Indexing in progress... {repo?.chunkCount || 0} chunks processed
        </div>
      )}

      {repo?.status === 'FAILED' && (
        <div className="status-message error">
          ❌ Indexing failed. Try indexing again.
        </div>
      )}

      {repo?.status !== 'READY' && (
        <div className="auto-refresh-toggle">
          <input
            type="checkbox"
            id="autoRefresh"
            checked={autoRefresh}
            onChange={(e) => setAutoRefresh(e.target.checked)}
          />
          <label htmlFor="autoRefresh">
            Auto-refresh status
          </label>
        </div>
      )}
    </div>
  )
}

export default RepositoryStatus
