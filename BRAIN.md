# BRAIN.md — Project Context (Single Source of Truth)

> Load this file first in every generation session. It defines WHAT the system is.
> Other files (PROMPT.md, SCHEMA.md, API.md, TASKS.md) reference this — do not repeat context, only point back here.

## 1. Project
**Name:** RepoSage — AI Code & Documentation Search Engine
**One-liner:** Index a GitHub repo (README + code + comments) into a vector DB; answer natural-language questions about the repo using RAG, citing file names/lines.

## 2. Stack (fixed — do not substitute)
| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x (Web, Data JPA) |
| DB | PostgreSQL 16 + `pgvector` extension |
| Vector search | pgvector (cosine similarity), fallback: Qdrant if pgvector unavailable |
| Embeddings | Gemini `text-embedding-004` (free tier) |
| LLM | Gemini `gemini-1.5-flash` (free tier) via REST |
| External API | GitHub REST API v3 (`/repos/{owner}/{repo}/contents`) |
| Build | Maven |
| Container | Docker + docker-compose (app + postgres) |
| Auth (optional, skip in MVP) | none |

## 3. Architecture (one paragraph)
User submits a GitHub repo URL → `IngestService` pulls files via GitHub API (README, `.java`, `.py`, `.js`, `.md` — skip binaries/tests/build folders) → `ChunkService` splits files into ~500-token chunks with overlap 50 → `EmbeddingService` embeds each chunk (Gemini) → chunks + vectors + metadata (file_path, repo_id, start_line, end_line) stored in `code_chunk` table (pgvector column) → user asks a question via `POST /api/query` → question embedded → top-k (default 5) nearest chunks retrieved via cosine similarity → chunks injected into prompt template → LLM generates answer citing file paths → response returned with answer + source citations.

## 4. Core entities
- **Repo**: id, owner, name, url, indexed_at, status(PENDING/INDEXING/READY/FAILED)
- **CodeChunk**: id, repo_id (FK), file_path, chunk_text, embedding(vector), start_line, end_line, chunk_type(README/CODE/COMMENT)
- **QueryLog** (optional, for demo/history): id, repo_id, question, answer, source_files(json), created_at

## 5. Conventions
- Package root: `com.reposage`
- Layers: `controller` → `service` → `repository` → `entity`/`dto`
- All external API calls (GitHub, Gemini) go through a dedicated `client` package, interfaces + impl, so they're mockable in tests.
- Config (API keys, model names, chunk size) in `application.yml`, overridable via env vars. NEVER hardcode keys.
- Every REST endpoint returns a consistent `ApiResponse<T>` wrapper: `{success, data, error}`.
- Errors: use `@ControllerAdvice` global exception handler.
- No frontend required for MVP — Postman/curl collection is the demo interface. (Stretch: minimal HTML/JS page.)

## 6. MVP scope boundary (do NOT build beyond this without being asked)
IN: repo ingestion, chunking, embedding, storage, single-turn Q&A with citations, basic caching of repeated questions.
OUT (stretch only, flag but don't build unless requested): multi-turn conversation memory, auth/multi-user, multiple vector DB backends, streaming responses, frontend UI, support for private repos.

## 7. Token-efficiency rule for generation
When generating code from this spec: produce ONE file per response unless files are trivially small (<15 lines) and tightly coupled (e.g. a DTO + its interface). Do not re-explain architecture already stated here — just write code with brief inline comments.
