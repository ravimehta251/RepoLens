# RepoLens — AI-Powered GitHub Repository Q&A

RepoLens is a Spring Boot application that uses Retrieval Augmented Generation (RAG) to answer natural-language questions about GitHub repositories. It indexes repository code and documentation, embeds them using Gemini, stores vectors in PostgreSQL with pgvector, and retrieves relevant code snippets to answer user queries.

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                     User / API Client                              │
└────────────────────────┬─────────────────────────────────────────┘
                         │
         ┌───────────────┴────────────────┐
         │                                │
    POST /api/repos                  POST /api/repos/{id}/query
    GET  /api/repos/{id}             GET  /api/repos/{id}/chunks
         │                                │
┌────────▼─────────────────────────────────▼──────────────┐
│         Spring Boot 3.x (RepoLensApplication)           │
├────────────────────────────────────────────────────────┤
│  Controllers:                                           │
│  ├─ RepoController     (indexing endpoints)            │
│  └─ QueryController    (query endpoints)               │
│                                                        │
│  Services:                                             │
│  ├─ IngestService      (orchestrate ingestion)         │
│  ├─ RagQueryService    (RAG pipeline)                  │
│  └─ ChunkService       (text chunking)                 │
│                                                        │
│  Clients (external APIs):                              │
│  ├─ GitHubClient       (fetch repo files)              │
│  ├─ EmbeddingClient    (Gemini text-embedding-004)     │
│  └─ LLMClient          (Gemini gemini-1.5-flash)       │
└────┬─────────────────────────────────────────────────┬─┘
     │                                                 │
     │                            ┌────────────────────┘
     │                            │
     │                    ┌───────▼────────┐
     │                    │   Gemini API   │
     │                    │ (embeddings &  │
     │                    │   chat)        │
     │                    └────────────────┘
     │
┌────▼──────────────────────────────────────┐
│         PostgreSQL 16 + pgvector          │
├───────────────────────────────────────────┤
│  Tables:                                  │
│  ├─ repo                                  │
│  ├─ code_chunk (with vector column)       │
│  └─ query_log                             │
│                                           │
│  Similarity search via cosine distance:   │
│  ORDER BY embedding <=> query_vector      │
└───────────────────────────────────────────┘
```

## Flow

1. **Ingestion** (async):
   - User submits GitHub URL via `POST /api/repos`
   - `IngestService` fetches files from GitHub REST API
   - `ChunkService` splits files into ~500-token chunks
   - `EmbeddingClient` embeds each chunk (Gemini)
   - Chunks + embeddings stored in `code_chunk` table
   - Repo status updates: PENDING → INDEXING → READY

2. **Query**:
   - User asks a question via `POST /api/repos/{id}/query`
   - Question embedded using Gemini
   - `CodeChunkRepository` finds top-5 nearest chunks (pgvector cosine similarity)
   - Chunks injected into prompt template
   - `LLMClient` generates answer via Gemini
   - Response includes answer + source file citations
   - Query logged for history

## Setup

### Prerequisites

- Java 17
- Maven 3.9+
- Docker & Docker Compose
- GitHub Personal Access Token (optional, for higher API rate limits)
- Gemini API Key (https://ai.google.dev)

### Environment Variables

Create a `.env` file or set in your shell:

```bash
# GitHub
GITHUB_TOKEN=github_pat_xxxxxxxxxxxx

# Gemini
GEMINI_API_KEY=AIzaXxxxxxxxxxxxxxxxxxxxxxXxXxXxxxx

# Database (optional, defaults shown)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=RepoLens_db
DB_USER=postgres
DB_PASSWORD=postgres

# Server
SERVER_PORT=8080
```

### Run with Docker Compose

```bash
# Clone and build
git clone <repo-url>
cd RepoLens
docker-compose up --build
```

The app will be available at `http://localhost:8080`.

### Run Locally

1. Start PostgreSQL with pgvector:
```bash
docker run --name pgvector -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=RepoLens_db -p 5432:5432 -v postgres_data:/var/lib/postgresql/data ankane/pgvector:latest
```

2. Run initialization SQL:
```bash
psql -h localhost -U postgres -d RepoLens_db -f init.sql
```

3. Build and run Spring Boot app:
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

### 1. Index a Repository

**Request:**
```bash
curl -X POST http://localhost:8080/api/repos \
  -H "Content-Type: application/json" \
  -d '{"githubUrl": "https://github.com/spring-projects/spring-boot"}'
```

**Response (202 Accepted):**
```json
{
  "success": true,
  "data": {
    "repoId": 1,
    "status": "PENDING"
  },
  "error": null
}
```

**Note:** Returns immediately; indexing happens asynchronously. Poll `/api/repos/{id}` to check status.

---

### 2. Check Repository Status

**Request:**
```bash
curl -X GET http://localhost:8080/api/repos/1 \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "repoId": 1,
    "owner": "spring-projects",
    "name": "spring-boot",
    "status": "READY",
    "chunkCount": 342
  },
  "error": null
}
```

**Status values:** `PENDING`, `INDEXING`, `READY`, `FAILED`

---

### 3. Query a Repository

**Request:**
```bash
curl -X POST http://localhost:8080/api/repos/1/query \
  -H "Content-Type: application/json" \
  -d '{"question": "How does Spring Boot handle dependency injection?"}'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "answer": "Spring Boot uses the Spring IoC container for dependency injection. Beans are automatically registered via @Component, @Service, @Repository annotations. Spring scans the classpath and constructor/setter injection is handled by AutowiredAnnotationBeanPostProcessor...",
    "sources": [
      {
        "filePath": "spring-boot-project/spring-boot/src/main/java/org/springframework/boot/SpringApplication.java",
        "startLine": 42,
        "endLine": 85
      },
      {
        "filePath": "README.md",
        "startLine": null,
        "endLine": null
      }
    ]
  },
  "error": null
}
```

---

### 4. List Chunks (Debug/Demo)

**Request:**
```bash
curl -X GET "http://localhost:8080/api/repos/1/chunks?limit=10" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "repoId": 1,
      "filePath": "README.md",
      "chunkText": "Spring Boot makes it easy...",
      "startLine": 1,
      "endLine": 15,
      "chunkType": "README",
      "createdAt": "2024-01-15T10:30:00"
    },
    ...
  ],
  "error": null
}
```

---

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
rag:
  chunk-size: 500        # tokens per chunk
  chunk-overlap: 50      # overlap between chunks
  top-k: 5               # top-k chunks for retrieval
  cache-ttl-minutes: 60  # query cache TTL

github:
  token: ${GITHUB_TOKEN}

gemini:
  api-key: ${GEMINI_API_KEY}
  embedding-model: text-embedding-004
  chat-model: gemini-1.5-flash
```

## Error Handling

| Status | Meaning | Example |
|---|---|---|
| 202 Accepted | Indexing started | `POST /api/repos` |
| 400 Bad Request | Invalid URL / malformed JSON | `{"githubUrl": "invalid"}` |
| 404 Not Found | Repo ID doesn't exist | Non-existent repo ID |
| 409 Conflict | Query on non-READY repo | Status != READY |
| 502 Bad Gateway | GitHub/Gemini API failure | API down/rate limit |

## Testing

```bash
# Unit tests (mocked clients)
mvn test

# Integration tests (requires live DB + APIs)
mvn verify -P integration-tests
```

## Development Notes

- **Threading:** `@Async` ingestion to avoid blocking HTTP response.
- **Chunking:** Text is split on word boundaries (greedy algorithm) to preserve semantics.
- **Embeddings:** Cached by normalized question hash (stretch feature, optional for MVP).
- **Vector index:** pgvector with `ivfflat` and cosine similarity (`<=>` operator).

## Troubleshooting

### "Repository is not ready for querying"
- Wait for `status` to be `READY` by polling `GET /api/repos/{id}`.
- Check logs: `docker logs RepoLens-app`.

### "Failed to embed"
- Verify `GEMINI_API_KEY` is set.
- Check Gemini API quota/limits.

### "Connection refused" (PostgreSQL)
- Ensure `docker-compose up` has completed and postgres is healthy.
- Check `docker ps` and logs: `docker logs RepoLens-postgres`.

## Future (Stretch Features)

- [ ] Query caching (Redis/Caffeine)
- [ ] Multi-turn conversation memory
- [ ] HTML/JS UI
- [ ] Support for private repos (OAuth)
- [ ] Streaming responses
- [ ] Alternative vector DBs (Qdrant, Weaviate)

## License

MIT
