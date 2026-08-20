# RepoSage - Complete Project Code Summary

## Project Overview

**RepoSage** is a Spring Boot 3.x application that implements an AI-powered GitHub repository Q&A system using Retrieval Augmented Generation (RAG). It fetches code from GitHub, chunks and embeds it using Google's Gemini API, stores vectors in PostgreSQL with pgvector, and answers natural language questions about repositories with citations.

## Technology Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.3.0
- **Database:** PostgreSQL 16 + pgvector
- **Vector DB:** pgvector (native PostgreSQL extension)
- **Embeddings API:** Google Gemini text-embedding-004
- **Chat/LLM:** Google Gemini gemini-1.5-flash
- **Build Tool:** Maven 3.9+
- **Container:** Docker + docker-compose
- **Cache:** Caffeine (optional, for query caching)

## Project Structure

```
reposage/
├── src/
│   ├── main/
│   │   ├── java/com/reposage/
│   │   │   ├── RepoSageApplication.java          # Main Spring Boot application
│   │   │   ├── controller/
│   │   │   │   ├── RepoController.java           # Repo indexing endpoints
│   │   │   │   ├── QueryController.java          # Query endpoints
│   │   │   │   └── GlobalExceptionHandler.java   # Error handling
│   │   │   ├── service/
│   │   │   │   ├── IngestService.java            # Async repo ingestion
│   │   │   │   ├── RagQueryService.java          # RAG query pipeline
│   │   │   │   └── ChunkService.java             # Text chunking logic
│   │   │   ├── client/
│   │   │   │   ├── GitHubClient.java             # GitHub API interface
│   │   │   │   ├── GitHubClientImpl.java          # GitHub API implementation
│   │   │   │   ├── EmbeddingClient.java          # Embedding interface
│   │   │   │   ├── EmbeddingClientImpl.java       # Gemini embedding implementation
│   │   │   │   ├── LLMClient.java                # LLM interface
│   │   │   │   └── LLMClientImpl.java             # Gemini LLM implementation
│   │   │   ├── entity/
│   │   │   │   ├── Repo.java                     # Repository entity
│   │   │   │   ├── CodeChunk.java                # Code chunk with vector
│   │   │   │   └── QueryLog.java                 # Query history (optional)
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java              # Response wrapper
│   │   │   │   ├── IndexRepoRequest.java         # Index request DTO
│   │   │   │   ├── IndexRepoResponse.java        # Index response DTO
│   │   │   │   ├── RepoStatusResponse.java       # Status response DTO
│   │   │   │   ├── QueryRequest.java             # Query request DTO
│   │   │   │   ├── QueryResponse.java            # Query response DTO
│   │   │   │   └── SourceRef.java                # Source citation DTO
│   │   │   ├── repository/
│   │   │   │   ├── RepoRepository.java           # Repo JPA repository
│   │   │   │   ├── CodeChunkRepository.java      # Chunk repository with vector search
│   │   │   │   └── QueryLogRepository.java       # Query log repository
│   │   │   ├── config/
│   │   │   │   └── CacheConfig.java              # Caffeine cache configuration
│   │   │   └── util/
│   │   │       └── GitHubUrlParser.java          # URL parsing utility
│   │   └── resources/
│   │       └── application.yml                   # Configuration
│   └── test/
│       └── java/com/reposage/service/
│           └── RagQueryServiceTest.java          # Service unit tests
├── pom.xml                                        # Maven configuration
├── Dockerfile                                     # Multi-stage Docker build
├── docker-compose.yml                             # Docker Compose orchestration
├── init.sql                                       # PostgreSQL initialization
├── README.md                                      # User guide
├── DEPLOYMENT.md                                  # Deployment guide
├── RepoSage.postman_collection.json              # Postman API collection
├── .env.example                                   # Environment variables template
└── .gitignore                                     # Git ignore patterns
```

## File Descriptions

### Core Application Files

#### `RepoSageApplication.java`
Main Spring Boot entry point. Enables async processing and configures RestTemplate with timeouts.

#### Controllers (`controller/`)

**RepoController.java**
- `POST /api/repos` - Index a GitHub repository (202 Accepted)
- `GET /api/repos/{id}` - Check indexing status and chunk count

**QueryController.java**
- `POST /api/repos/{id}/query` - Ask questions about indexed repo
- `GET /api/repos/{id}/chunks` - Debug endpoint to view raw chunks

**GlobalExceptionHandler.java**
- Centralized error handling with appropriate HTTP status codes
- Returns consistent `ApiResponse<T>` format

### Services (`service/`)

**IngestService.java**
- Orchestrates async repo indexing pipeline
- Fetches from GitHub → chunks → embeds → stores
- Updates repo status: PENDING → INDEXING → READY/FAILED
- Uses `@Async` to return HTTP response immediately

**RagQueryService.java**
- Embeds question → searches top-k chunks → builds prompt → calls LLM
- Extracts source citations from retrieved chunks
- Logs queries to query_log table

**ChunkService.java**
- Splits file content into ~500-token chunks with 50-token overlap
- Handles line tracking for code files
- Detects chunk type (README vs CODE)

### Clients (`client/`)

**GitHubClient interface & GitHubClientImpl**
- Fetches repository files via GitHub REST API v3
- Filters: `.md`, `.java`, `.py`, `.js`, `.ts`, `.jsx`, `.tsx`, `.css`, `.html`
- Skips: `test/`, `tests/`, `target/`, `node_modules/`, `.git/`, `build/`, `dist/`, `out/`
- Returns `List<FetchedFile>` with path and content

**EmbeddingClient interface & EmbeddingClientImpl**
- Calls Gemini `text-embedding-004` API
- Returns `Vector` (pgvector type) or `float[]`
- Configured in `application.yml`

**LLMClient interface & LLMClientImpl**
- Calls Gemini `gemini-1.5-flash` API for chat completion
- Takes prompt string, returns answer text

### Data Entities (`entity/`)

**Repo.java**
- `id` (Long, PK)
- `owner`, `name`, `url` (unique)
- `status` (PENDING, INDEXING, READY, FAILED)
- `indexed_at`, `created_at` (timestamps)

**CodeChunk.java**
- `id` (Long, PK)
- `repo_id` (FK)
- `file_path`, `chunk_text`
- `embedding` (Vector type, 768 dimensions)
- `start_line`, `end_line` (for code files)
- `chunk_type` (README, CODE, COMMENT)
- Indices: repo_id, ivfflat on embedding vector

**QueryLog.java** (optional)
- `id`, `repo_id` (FK)
- `question`, `answer`, `source_files` (JSON)
- `created_at`

### Data Transfer Objects (`dto/`)

- **ApiResponse<T>** - Wrapper for all responses: `{success, data, error}`
- **IndexRepoRequest** - `{githubUrl}`
- **IndexRepoResponse** - `{repoId, status}`
- **RepoStatusResponse** - `{repoId, owner, name, status, chunkCount}`
- **QueryRequest** - `{question}`
- **QueryResponse** - `{answer, sources[]}`
- **SourceRef** - `{filePath, startLine, endLine}`

### Repositories (`repository/`)

- **RepoRepository** - JPA repository for Repo entities
- **CodeChunkRepository** - Native query for vector similarity search
  - `findSimilarChunks(repoId, queryEmbedding, k)` - Uses pgvector `<=>` operator
- **QueryLogRepository** - JPA repository for QueryLog

### Configuration

**application.yml**
- Database connection: PostgreSQL 16 on localhost:5432
- GitHub API: token from env var
- Gemini API: key and model names from env vars
- RAG settings: chunk-size (500), overlap (50), top-k (5), cache TTL (60 min)

**CacheConfig.java**
- Caffeine cache for query results
- Max 1000 entries, 1-hour TTL
- Optional for MVP, enables `@Cacheable` on query methods

### Database

**init.sql**
- Creates `vector` extension
- Creates `repo`, `code_chunk`, `query_log` tables
- Creates indices: repo_id, ivfflat on embedding
- Uses `ON DELETE CASCADE` for repo relationship

### Build & Deployment

**pom.xml**
- Spring Boot 3.3.0 parent, Java 17
- Dependencies: web, data-jpa, postgresql driver, pgvector-java, validation, lombok, webflux, jackson, actuator, cache, caffeine
- Maven build with spring-boot-maven-plugin

**Dockerfile**
- Two-stage build: Maven build stage + JDK 17 runtime
- Exposes port 8080
- Runs `java -jar app.jar`

**docker-compose.yml**
- `postgres` service: ankane/pgvector image, port 5432, volume mount for data
- `reposage` service: builds from Dockerfile, port 8080, depends on postgres
- Environment variables passed from `.env`

### Documentation

**README.md**
- Architecture diagram (ASCII)
- Flow explanation (ingestion + query)
- Setup: prerequisites, environment variables, Docker Compose, local run
- API endpoints with curl examples
- Configuration options
- Error handling table
- Troubleshooting guide
- Future (stretch) features

**DEPLOYMENT.md**
- Quick start with docker-compose
- Manual local deployment steps
- Kubernetes YAML example
- Performance tuning (database, application)
- Monitoring: logs, health checks, metrics
- Troubleshooting: common issues and solutions
- Scaling strategies: horizontal, database
- Backup & restore procedures
- Security considerations
- Maintenance tasks

**RepoSage.postman_collection.json**
- Postman collection with 4 endpoints for testing
- Ready to import into Postman

### Utilities

**GitHubUrlParser.java**
- Parses GitHub URLs: `https://github.com/owner/repo[.git]`
- Returns `GitHubRepo` record with owner and name
- Throws `IllegalArgumentException` for invalid URLs

### Testing

**RagQueryServiceTest.java**
- Unit tests for RagQueryService
- Mocked dependencies: repositories, clients
- Tests: successful query, not-ready status, repo not found
- Uses JUnit 5 + Mockito

## API Endpoints Summary

| Method | Endpoint | Status | Purpose |
|--------|----------|--------|---------|
| POST | `/api/repos` | 202 | Index a GitHub repo (async) |
| GET | `/api/repos/{id}` | 200 | Check indexing status |
| POST | `/api/repos/{id}/query` | 200 | Ask question about repo |
| GET | `/api/repos/{id}/chunks` | 200 | Debug: view chunks |

## Key Design Decisions

1. **Async Ingestion** - `@Async` on IngestService to avoid blocking HTTP response
2. **Native Vector Query** - Uses pgvector's `<=>` operator for cosine similarity (faster than SQL)
3. **Consistent Response Format** - All endpoints return `ApiResponse<T>` wrapper
4. **Dependency Injection** - Client interfaces allow easy mocking for tests
5. **Config via Environment** - All secrets/API keys from `application.yml` + env vars
6. **Error Handling** - Global `@ControllerAdvice` for uniform error responses
7. **Stateless Design** - All replicas can read/write to same database
8. **Chunking Strategy** - Word-boundary breaks to preserve code semantics
9. **Optional Caching** - Caffeine cache for repeated questions (stretch feature)
10. **Docker Ready** - Multi-stage build and docker-compose for easy deployment

## Environment Variables Required

```
GITHUB_TOKEN=<personal access token>
GEMINI_API_KEY=<Gemini API key from ai.google.dev>
DB_HOST=postgres (or localhost for local dev)
DB_PORT=5432
DB_NAME=reposage_db
DB_USER=postgres
DB_PASSWORD=postgres
SERVER_PORT=8080
```

## Running the Application

### Docker Compose (Recommended)
```bash
docker-compose up --build
# App available at http://localhost:8080
```

### Local Development
```bash
# Start PostgreSQL with pgvector
docker run --name pgvector -e POSTGRES_PASSWORD=postgres -p 5432:5432 ankane/pgvector:latest

# Build and run
mvn clean install
mvn spring-boot:run
```

## Testing

```bash
# Unit tests (mocked)
mvn test

# Package for deployment
mvn clean package
```

## Next Steps (Stretch Features)

1. Add HTML/JS frontend UI
2. Implement query caching with Redis
3. Support multi-turn conversations
4. Add authentication (OAuth2/JWT)
5. Support private repositories
6. Implement streaming responses
7. Add support for alternative vector DBs (Qdrant, Weaviate)
8. Rate limiting and API key authentication
9. Query performance monitoring and optimization
10. Support for multiple languages (Python AST parsing, etc.)

---

This complete codebase implements the MVP scope as defined in BRAIN.md, ready for deployment and further enhancement.
