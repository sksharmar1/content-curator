# AI Content Curator

A full-stack AI-powered content curation platform for self-directed learners.

## Architecture

- **user-profile** (Spring Boot, port 8081) — JWT auth, interest graph
- **content-ingestion** (Spring Boot, port 8082) — RSS polling, deduplication
- **recommendation** (Spring Boot, port 8083) — scoring engine, ranked feed
- **analytics** (Spring Boot, port 8084) — reads, notes, VADER sentiment
- **ml-service** (Python Flask, port 5001) — VADER + TF-IDF recommender
- **frontend** (React + TypeScript, port 3000) — personalised UI

## Quick Start

```bash
# Start infrastructure
cd docker && docker compose up -d

# Start services (one per terminal)
cd user-profile && mvn spring-boot:run
cd content-ingestion && mvn spring-boot:run
cd recommendation && mvn spring-boot:run
cd analytics && mvn spring-boot:run
cd ml-service && source venv/bin/activate && python3 main.py

# Start frontend
cd frontend && npm start
```

## Tech Stack

Java 21, Spring Boot 3.2, Python 3.11, React 18, TypeScript,
PostgreSQL 16, AWS SQS, AWS S3, AWS Fargate, AWS Lambda,
OpenShift, Docker, scikit-learn, NLTK/VADER, JJWT

## Patterns

Domain-Driven Design · Hexagonal Architecture · Event-Driven Architecture ·
Multi-module Maven · Microservices · JWT Auth · TF-IDF Recommendations
