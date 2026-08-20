# API.md — REST Endpoint Contracts

All responses wrapped: `{ "success": bool, "data": <T|null>, "error": <string|null> }`

## POST /api/repos
Index a new repo.
**Request:**
```json
{ "githubUrl": "https://github.com/owner/repo" }
```
**Response (202 Accepted):**
```json
{ "success": true, "data": { "repoId": 1, "status": "PENDING" }, "error": null }
```
Triggers async ingestion (GitHub pull → chunk → embed → store). Client polls status.

## GET /api/repos/{id}
Check indexing status.
**Response:**
```json
{ "success": true, "data": { "repoId": 1, "owner": "...", "name": "...", "status": "READY", "chunkCount": 342 }, "error": null }
```

## POST /api/repos/{id}/query
Ask a question about the indexed repo.
**Request:**
```json
{ "question": "How does authentication work in this repo?" }
```
**Response:**
```json
{
  "success": true,
  "data": {
    "answer": "Authentication is handled in AuthService.java using JWT tokens...",
    "sources": [
      { "filePath": "src/main/java/com/app/AuthService.java", "startLine": 12, "endLine": 45 },
      { "filePath": "README.md", "startLine": null, "endLine": null }
    ]
  },
  "error": null
}
```
Flow: embed question → pgvector top-5 similarity search scoped to repo_id → build prompt (template in TASKS.md Task 6) → call Gemini → parse answer → return with sources. Cache by (repo_id, normalized question hash) in Redis or in-memory Caffeine cache, TTL 1h — optional for MVP, mark as stretch if time-constrained.

## GET /api/repos/{id}/chunks (debug/demo endpoint, optional)
Returns raw stored chunks for inspection — useful for interview demo to show retrieval is working.

## Error responses
| Status | Case |
|---|---|
| 400 | invalid GitHub URL / malformed request |
| 404 | repo id not found |
| 409 | query attempted while status != READY |
| 502 | upstream GitHub/Gemini API failure |
