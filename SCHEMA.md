# SCHEMA.md — Database Schema

PostgreSQL 16 + `pgvector` extension (`CREATE EXTENSION IF NOT EXISTS vector;`)

## Table: repo
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| owner | VARCHAR(255) | GitHub owner/org |
| name | VARCHAR(255) | repo name |
| url | VARCHAR(500) | full GitHub URL, unique |
| status | VARCHAR(20) | PENDING / INDEXING / READY / FAILED |
| indexed_at | TIMESTAMP | nullable until READY |
| created_at | TIMESTAMP | default now() |

## Table: code_chunk
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| repo_id | BIGINT FK → repo.id | ON DELETE CASCADE |
| file_path | VARCHAR(1000) | e.g. `src/main/java/.../AuthService.java` |
| chunk_text | TEXT | raw chunk content |
| embedding | VECTOR(768) | Gemini text-embedding-004 output dim |
| start_line | INT | nullable for README/non-code |
| end_line | INT | nullable |
| chunk_type | VARCHAR(20) | README / CODE / COMMENT |
| created_at | TIMESTAMP | default now() |

Index: `CREATE INDEX ON code_chunk USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);`

## Table: query_log (optional, MVP-stretch)
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| repo_id | BIGINT FK → repo.id | |
| question | TEXT | |
| answer | TEXT | |
| source_files | JSONB | array of {file_path, start_line, end_line} |
| created_at | TIMESTAMP | default now() |

## JPA notes
- `CodeChunk.embedding` maps via `pgvector-java` library's `Vector` type (add dependency: `com.pgvector:pgvector:0.1.6`).
- Similarity query is a native query (JPQL doesn't support `<=>` operator):
```sql
SELECT * FROM code_chunk
WHERE repo_id = :repoId
ORDER BY embedding <=> :queryEmbedding
LIMIT :k;
```
