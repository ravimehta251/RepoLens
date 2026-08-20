import { useState, useRef, useEffect } from 'react'
import { FiSend } from 'react-icons/fi'
import './QueryInterface.css'

function QueryInterface({ onSubmit, loading }) {
  const [question, setQuestion] = useState('')
  const textareaRef = useRef(null)

  useEffect(() => {
    // Auto-resize textarea
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto'
      textareaRef.current.style.height = textareaRef.current.scrollHeight + 'px'
    }
  }, [question])

  const handleSubmit = (e) => {
    e.preventDefault()
    if (question.trim() && !loading) {
      onSubmit(question.trim())
      setQuestion('')
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
      handleSubmit(e)
    }
  }

  return (
    <form className="query-interface" onSubmit={handleSubmit}>
      <div className="input-group">
        <textarea
          ref={textareaRef}
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask a question about the repository... (Ctrl+Enter to submit)"
          disabled={loading}
          className="query-textarea"
          rows={3}
        />
        <small className="input-hint">
          Tip: Ctrl+Enter to submit quickly
        </small>
      </div>

      <div className="button-group">
        <button
          type="submit"
          disabled={!question.trim() || loading}
          className="btn btn-submit"
        >
          <FiSend />
          Ask Question
        </button>
      </div>
    </form>
  )
}

export default QueryInterface
