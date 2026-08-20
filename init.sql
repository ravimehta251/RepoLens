-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create repo table
CREATE TABLE IF NOT EXISTS repo (
    id BIGSERIAL PRIMARY KEY,
    owner VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    indexed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create code_chunk table
CREATE TABLE IF NOT EXISTS code_chunk (
    id BIGSERIAL PRIMARY KEY,
    repo_id BIGINT NOT NULL REFERENCES repo(id) ON DELETE CASCADE,
    file_path VARCHAR(1000) NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding vector(768),
    start_line INT,
    end_line INT,
    chunk_type VARCHAR(20) NOT NULL DEFAULT 'CODE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indices
CREATE INDEX IF NOT EXISTS idx_code_chunk_repo_id ON code_chunk(repo_id);
CREATE INDEX IF NOT EXISTS idx_code_chunk_embedding ON code_chunk USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Create query_log table
CREATE TABLE IF NOT EXISTS query_log (
    id BIGSERIAL PRIMARY KEY,
    repo_id BIGINT NOT NULL REFERENCES repo(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    source_files JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_query_log_repo_id ON query_log(repo_id);
