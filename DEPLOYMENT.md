# RepoSage Deployment Guide

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- GitHub Personal Access Token
- Gemini API Key

## Quick Start (Docker Compose)

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd reposage
   ```

2. **Create `.env` file:**
   ```bash
   cp .env.example .env
   # Edit .env and add your credentials
   ```

3. **Start services:**
   ```bash
   docker-compose up --build
   ```

4. **Verify services:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## Manual Deployment (Local)

### 1. PostgreSQL Setup

```bash
# Start PostgreSQL with pgvector
docker run --name pgvector \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=reposage_db \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  -d ankane/pgvector:latest

# Initialize database
psql -h localhost -U postgres -d reposage_db < init.sql
```

### 2. Build Application

```bash
mvn clean package -DskipTests
```

### 3. Run Application

```bash
export GITHUB_TOKEN=your_token_here
export GEMINI_API_KEY=your_key_here
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=reposage_db
export DB_USER=postgres
export DB_PASSWORD=postgres

java -jar target/reposage-1.0.0.jar
```

## Production Deployment

### Kubernetes (Optional)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reposage
spec:
  replicas: 2
  selector:
    matchLabels:
      app: reposage
  template:
    metadata:
      labels:
        app: reposage
    spec:
      containers:
      - name: reposage
        image: your-registry/reposage:latest
        ports:
        - containerPort: 8080
        env:
        - name: GITHUB_TOKEN
          valueFrom:
            secretKeyRef:
              name: reposage-secrets
              key: github-token
        - name: GEMINI_API_KEY
          valueFrom:
            secretKeyRef:
              name: reposage-secrets
              key: gemini-api-key
        - name: DB_HOST
          value: postgres-service
        - name: DB_PORT
          value: "5432"
        - name: DB_NAME
          value: reposage_db
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

### Performance Tuning

#### Database Optimization
```sql
-- Analyze table for query planner
ANALYZE code_chunk;

-- Rebuild indices if degraded
REINDEX INDEX idx_code_chunk_embedding;

-- Monitor vector search performance
EXPLAIN ANALYZE
SELECT * FROM code_chunk
WHERE repo_id = 1
ORDER BY embedding <=> '[0.1, 0.2, ...]'::vector
LIMIT 5;
```

#### Application Settings
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true

rag:
  chunk-size: 500      # Tune based on domain
  chunk-overlap: 50
  top-k: 5
  cache-ttl-minutes: 60
```

## Monitoring

### Logs
```bash
# Docker logs
docker logs -f reposage-app

# Application logs
tail -f logs/application.log
```

### Health Checks
```bash
# Spring Actuator endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

### Database Connection Monitoring
```sql
-- Check active connections
SELECT datname, count(*) FROM pg_stat_activity GROUP BY datname;

-- Check slow queries
SELECT query, mean_exec_time FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 5;
```

## Troubleshooting

### Container won't start
```bash
docker logs reposage-app
# Check if port 8080 is in use
lsof -i :8080
```

### Database connection timeout
```bash
# Test connectivity
docker exec reposage-app nc -zv postgres 5432
```

### Out of memory
```bash
# Increase JVM heap
docker-compose up --build -e JAVA_OPTS="-Xmx2g -Xms1g"
```

### Slow queries
1. Check pgvector index configuration
2. Increase `top-k` retrieval carefully (performance vs. accuracy trade-off)
3. Monitor with `EXPLAIN ANALYZE`

## Scaling

### Horizontal Scaling
- Use multiple app replicas behind a load balancer
- All replicas connect to the same PostgreSQL instance
- Read replicas optional for GET endpoints

### Database Scaling
- Upgrade PostgreSQL instance resources
- Enable connection pooling (pgBouncer)
- Archive old query logs to separate storage

## Backup & Restore

```bash
# Backup database
docker exec reposage-postgres pg_dump -U postgres reposage_db > backup.sql

# Restore database
docker exec -i reposage-postgres psql -U postgres reposage_db < backup.sql

# Backup volumes
docker run --rm -v reposage_postgres_data:/data -v $(pwd):/backup \
  alpine tar czf /backup/postgres_backup.tar.gz -C /data .
```

## Security Considerations

1. **API Keys:**
   - Use environment variables or secrets manager (AWS Secrets, HashiCorp Vault)
   - Rotate keys regularly
   - Never commit keys to version control

2. **Database:**
   - Enable SSL/TLS for PostgreSQL connections
   - Use strong passwords
   - Restrict network access (firewall rules)

3. **API Endpoints:**
   - Add authentication layer (OAuth2, JWT) for production
   - Implement rate limiting
   - Add API key validation

4. **GitHub Token:**
   - Use personal access tokens with minimal scopes
   - Consider organization tokens for enterprise repos

## Maintenance

### Regular Tasks
- Monitor disk usage (embeddings + code chunks grow over time)
- Review and archive old query logs
- Update dependencies monthly
- Rotate secrets/API keys

### Update Process
```bash
# Test update in staging
docker-compose up --build

# Tag and push
docker tag reposage:latest your-registry/reposage:v2.0.0
docker push your-registry/reposage:v2.0.0

# Rolling update
docker-compose pull
docker-compose up -d
```

## Support

For issues or questions:
1. Check logs: `docker logs -f reposage-app`
2. Review README.md
3. Open an issue on GitHub
4. Check Gemini API status: https://ai.google.dev/
