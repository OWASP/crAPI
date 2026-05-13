# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Purpose

crAPI (completely ridiculous API) is an **intentionally vulnerable** web application for practicing OWASP API Top 10 security testing. Vulnerabilities are features, not bugs. Do not "fix" security issues unless explicitly asked.

## Running the Full Stack

All services run as Docker containers. The compose file lives in `deploy/docker/`.

```bash
# Start (localhost-only binding, default)
cd deploy/docker && docker compose -f docker-compose.yml --compatibility up -d

# Start (all interfaces, e.g. for VM access)
cd deploy/docker && LISTEN_IP="0.0.0.0" docker compose -f docker-compose.yml --compatibility up -d

# Stop
cd deploy/docker && docker compose down

# Stop and wipe all persistent data
cd deploy/docker && docker compose down -v

# View logs for a service
cd deploy/docker && docker compose logs -f crapi-identity

# Status
cd deploy/docker && docker compose ps
```

The interactive `./crapi-dev.sh` script at the repo root wraps all of the above with a numbered menu.

**Access points when running:**
- Web UI: http://localhost:8888
- Mailhog (email): http://localhost:8025
- Identity API (only externally exposed service): http://localhost:8090

**Default credentials:** `admin@example.com` / `Admin!123`

## Building Images from Source

```bash
# Build all service images
cd deploy/docker && bash build-all.sh
```

Each service also has its own `build-image.sh` / `build-image.bat` in `services/<name>/`.

## Per-Service Development

### Identity (Java 17 / Spring Boot 3.2, Gradle)
```bash
cd services/identity
./gradlew build          # compile + package
./gradlew test           # run tests
./gradlew spotlessCheck  # lint check
./gradlew spotlessApply  # auto-fix formatting
```
Uses Google Java Format via Spotless. Spring Security + `io.jsonwebtoken` for JWT.

### Community (Go)
```bash
cd services/community
go build        # compile
go test ./...   # run all tests
go mod tidy     # sync dependencies
```

### Workshop (Python / Django)
```bash
cd services/workshop
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
python manage.py migrate
python manage.py test           # run tests
black . --check                 # lint check
black .                         # auto-fix formatting
```
The Django project is split into `crapi_site/` (settings/config) and apps under `crapi/` (mechanic, merchant, shop, user).

### Chatbot (Python / Quart + LangChain)
```bash
cd services/chatbot
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
pip install -r requirements-dev.txt   # dev extras
```
Requires `CHATBOT_OPENAI_API_KEY` environment variable to be set for LLM features. Uses `DEFAULT_MODEL=gpt-4o-mini`.

### Web (React 18 / TypeScript)
```bash
cd services/web
npm install
npm run build                    # production build
npm test -- --watchAll=false     # run tests (CI mode)
npm run lint                     # prettier check
npm run lint:fix                 # auto-fix formatting
```

## Configuration

`deploy/docker/.env` controls deployment-level settings. Notable toggles:

| Variable | Default | Effect |
|---|---|---|
| `LISTEN_IP` | `127.0.0.1` | Network interface binding |
| `ENABLE_SHELL_INJECTION` | `false` | Enables shell injection vulnerability |
| `ENABLE_LOG4J` | `false` | Enables Log4Shell vulnerability |
| `TLS_ENABLED` | `true` | Enable TLS across services |

Custom JWKS keys: place `jwks.json` in `deploy/docker/keys/` (or equivalent path for other deploy methods).

## Architecture Overview

All services communicate on an internal Docker network. Only `crapi-web` (port 8888/8443) and `crapi-identity` (port 8090) are port-forwarded to the host.

```
Browser → crapi-web (OpenResty/Nginx)
              ↓ reverse-proxy routes to:
    ┌─────────────────────────────────────┐
    │  crapi-identity  (Java/Spring Boot) │ ← JWT issuance, user/vehicle mgmt
    │  crapi-community (Go)               │ ← Blog/social posts & comments
    │  crapi-workshop  (Python/Django)    │ ← Mechanic/shop/orders
    │  crapi-chatbot   (Python/Quart)     │ ← LLM chatbot (LangChain + OpenAI)
    └─────────────────────────────────────┘
              ↓
    ┌──────────────────────────────────────┐
    │  postgresdb  (PostgreSQL 14)         │ ← used by identity, community, workshop
    │  mongodb     (MongoDB 4.4)           │ ← used by community, workshop, chatbot, mailhog
    │  chromadb    (ChromaDB)              │ ← vector store for chatbot RAG
    │  mailhog                             │ ← SMTP trap; all @example.com email goes here
    └──────────────────────────────────────┘

External simulation:
    api.mypremiumdealership.com  (Go, HTTPS-only)
        → /v1/vin/ownership   – VIN lookup (Basic Auth: vendorcrapi / Pa$$4Vendor_1)
        → /v1/payment         – Payment info
```

**JWT flow:** Identity service issues JWTs. Workshop and Community validate them by calling Identity's `/identity/api/auth/verify` endpoint over the internal Docker network (configured via `IDENTITY_SERVICE` env var). They do not verify the JWT signature locally.

**Gateway service** (`api.mypremiumdealership.com`) simulates an external dealership API. It generates deterministic fake PII (VIN owners, payment cards) seeded by input values using `github.com/jaswdr/faker`. It is intentionally accessible from the Workshop service to enable SSRF challenges.

**Web frontend** (`services/web/src/`) uses Redux + Redux-Saga for state/async. The `sagas/` directory handles all API calls. OpenResty proxies API paths to the appropriate backend container using `nginx.conf.template`.

## API Documentation

- OpenAPI spec: `openapi-spec/crapi-openapi-spec.json`
- Postman collections: `postman_collections/`

## Kubernetes / Helm

Helm charts live in `deploy/helm/`. For Minikube, use `values-pv.yaml` with `hostPath` configured. Access on `<LOADBALANCER_IP>:8888` or `http://$(minikube ip):30080`.

## Vulnerability Challenges

18 documented challenges in `docs/challenges.md`, covering: BOLA, Broken Authentication, Excessive Data Exposure, Rate Limiting, BFLA, Mass Assignment, SSRF, NoSQL Injection, SQL Injection, Unauthenticated Access, JWT Vulnerabilities, and LLM/Chatbot (prompt injection, credential extraction, action hijacking). Three additional secret challenges exist.
