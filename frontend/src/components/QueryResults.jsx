import { useState } from 'react'
import { FiChevronDown, FiChevronUp, FiCopy } from 'react-icons/fi'
import './QueryResults.css'

function QueryResults({ results }) {
  const [expandedSources, setExpandedSources] = useState({})
  const [copiedId, setCopiedId] = useState(null)

  const toggleSource = (index) => {
    setExpandedSources((prev) => ({
      ...prev,
      [index]: !prev[index]
    }))
  }

  const copyToClipboard = (text, id) => {
    navigator.clipboard.writeText(text)
    setCopiedId(id)
    setTimeout(() => setCopiedId(null), 2000)
  }

  const sourceRefs = results?.sourceReferences || []

  return (
    <div className="query-results">
      <div className="answer-section">
        <h3>Answer</h3>
        <div className="answer-text">
          {results?.answer}
        </div>
      </div>

      {sourceRefs.length > 0 && (
        <div className="sources-section">
          <h3>Source References ({sourceRefs.length})</h3>

          <div className="sources-list">
            {sourceRefs.map((source, index) => (
              <div key={index} className="source-card">
                <button
                  className="source-header"
                  onClick={() => toggleSource(index)}
                >
                  <div className="source-title">
                    <span className="source-file">📄 {source.filePath}</span>
                    {source.startLine && (
                      <span className="source-lines">
                        Lines {source.startLine}-{source.endLine || source.startLine}
                      </span>
                    )}
                  </div>
                  <div className="source-chevron">
                    {expandedSources[index] ? <FiChevronUp /> : <FiChevronDown />}
                  </div>
                </button>

                {expandedSources[index] && source.chunkText && (
                  <div className="source-content">
                    <pre><code>{source.chunkText}</code></pre>
                    <button
                      className="btn-copy"
                      onClick={() => copyToClipboard(source.chunkText, `copy-${index}`)}
                      title="Copy to clipboard"
                    >
                      <FiCopy />
                      {copiedId === `copy-${index}` ? 'Copied!' : 'Copy'}
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="results-footer">
        <small>
          💡 Tip: Click on source references to view the actual code
        </small>
      </div>
    </div>
  )
}

export default QueryResults
