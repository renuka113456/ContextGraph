# ContextGraph Backend

Spring Boot backend foundation for ContextGraph.

## Stack
Java 17 · Spring Boot · PostgreSQL · JPA · JWT Security · Apache Tika

## Included
- JWT register/login
- BCrypt passwords
- Protected REST API
- Document upload and 25 MB limit
- SHA-256 duplicate detection
- PDF/DOCX/TXT extraction through Apache Tika
- Keyword/date/entity/topic metadata extraction
- Context-aware hybrid search foundation
- Knowledge-graph node/relation models
- Dashboard and health APIs

## Local setup
1. Install JDK 17+ and Maven 3.9+.
2. Install PostgreSQL and create database `contextgraph`.
3. Update DB_USERNAME/DB_PASSWORD if needed.
4. Run `mvn spring-boot:run`.
5. API: http://localhost:8080
6. Health: http://localhost:8080/api/health

## Environment variables
DB_URL=jdbc:postgresql://localhost:5432/contextgraph
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=use-a-long-random-secret-of-at-least-32-characters
CORS_ALLOWED_ORIGINS=http://localhost:5173
STORAGE_DIR=./storage

## Endpoints
POST /api/auth/register
POST /api/auth/login
GET /api/documents
POST /api/documents/upload
GET /api/documents/{id}
DELETE /api/documents/{id}
GET /api/search?q=AI%20internship%20last%20month
GET /api/search/understanding?q=AI%20internship%20last%20month
GET /api/graph/nodes
GET /api/graph/relations/{id}
GET /api/dashboard/summary
GET /api/health

## Next layer
Real embeddings/vector retrieval, OCR, relationship construction, context clustering, memory trails, conflict/freshness detection, explainable context reconstruction, and evaluation metrics.
