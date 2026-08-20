# TASKS.md — Build Order

Work top to bottom. One task = one AI response (see PROMPT.md rules). Each task lists its output file(s).

| ID | Task | Output file(s) |
|---|---|---|
| T1 | Maven project skeleton: `pom.xml` with Spring Web, Data JPA, PostgreSQL driver, pgvector-java, validation, lombok | `pom.xml` |
| T2 | `application.yml` — datasource, server port, placeholders for `github.token`, `gemini.api-key`, `gemini.embedding-model`, `gemini.chat-model`, `rag.chunk-size`, `rag.chunk-overlap`, `rag.top-k` | `application.yml` |
| T3 | Entities per SCHEMA.md: `Repo`, `CodeChunk`, `QueryLog` | `Repo.java`, `CodeChunk.java`, `QueryLog.java` |
| T4 | Repositories: `RepoRepository`, `CodeChunkRepository` (with native similarity query method) | `RepoRepository.java`, `CodeChunkRepository.java` |
| T5 | DTOs: request/response shapes from API.md (`IndexRepoRequest`, `QueryRequest`, `QueryResponse`, `SourceRef`, `ApiResponse<T>`) | `dto/*.java` |
| T6 | `GitHubClient` interface + impl — fetch repo tree, filter to README/.java/.py/.js/.md, skip `test/`, `target/`, `node_modules/`, `.git/`; return `List<FetchedFile>` (path, content) | `GitHubClient.java`, `GitHubClientImpl.java` |
| T7 | `ChunkService` — split file content into ~`chunk-size` token chunks with `chunk-overlap`, tag `chunk_type`, track start/end line for code files | `ChunkService.java` |
| T8 | `EmbeddingClient` interface + impl — call Gemini embedding endpoint, return `float[]`/`Vector` | `EmbeddingClient.java`, `EmbeddingClientImpl.java` |
| T9 | `LLMClient` interface + impl — call Gemini chat endpoint with a prompt string, return answer text | `LLMClient.java`, `LLMClientImpl.java` |
| T10 | `IngestService` — orchestrates T6→T7→T8→save CodeChunk rows, updates Repo.status through PENDING→INDEXING→READY/FAILED, runs `@Async` | `IngestService.java` |
| T11 | `RagQueryService` — embed question, call `CodeChunkRepository` similarity query, build prompt (template below), call `LLMClient`, parse into `QueryResponse`, log to `QueryLog` | `RagQueryService.java` |
| T12 | `RepoController` — `POST /api/repos`, `GET /api/repos/{id}` | `RepoController.java` |
| T13 | `QueryController` — `POST /api/repos/{id}/query`, `GET /api/repos/{id}/chunks` | `QueryController.java` |
| T14 | `GlobalExceptionHandler` (`@ControllerAdvice`) mapping errors per API.md table | `GlobalExceptionHandler.java` |
| T15 | `docker-compose.yml` (app + postgres w/ pgvector image `ankane/pgvector`) + `Dockerfile` | `docker-compose.yml`, `Dockerfile` |
| T16 | `README.md` — setup steps, env vars needed, curl examples for all 4 endpoints, architecture diagram (ASCII ok) | `README.md` |
| T17 (stretch) | Caffeine/Redis cache on query endpoint | `CacheConfig.java` |
| T18 (stretch) | Minimal single-page HTML/JS demo UI (paste repo URL, ask question, see answer + sources) | `static/index.html` |

## Prompt template (used in T11)
```
You are a code assistant answering questions about the GitHub repository "{owner}/{name}".
Use ONLY the following retrieved context to answer. If the context is insufficient, say so.

Context:
{for each source chunk: "--- {file_path} (lines {start}-{end}) ---\n{chunk_text}\n"}

Question: {question}

Answer concisely. Cite file paths inline where relevant.
```
