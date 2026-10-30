# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

`AGENTS.md` (also auto-loaded) covers the stack-level basics: compose lifecycle commands, per-service build/lint commands, service port map, and the vulnerability toggles in `deploy/docker/.env`. This file covers what that leaves out — local-setup gotchas, test invocation, and the cross-file architecture.

## This is an intentionally vulnerable app

crAPI exists to be exploited (OWASP API Top 10 training). Vulnerabilities are features. Never "harden" code unless explicitly asked — removing a BOLA check or an unsanitized query breaks a documented challenge in `docs/challenges.md`.

This is a fork of OWASP/crAPI, hosted on GitLab (`origin`), meant to be a one-and-done lab: clone, optionally add `deploy/docker/llm.env.local`, run `./quickstart.sh`. **Work on `develop`**; `main` is protected and only takes merge requests from `develop`. Never commit to or push `main` directly. Releases are `vX.Y.Z` tags on `main`, and the tag pipeline creates the GitLab release from that version's `CHANGELOG.md` section. CI is `.gitlab-ci.yml`: shellcheck, bash 3.2 syntax, compose validation, Python compile, secret detection, and a chatbot image build. `.github/workflows` is upstream's and doesn't run on GitLab. Fork-specific additions at the repo root: `quickstart.sh` (non-interactive deploy, `--vulnerable` / `--localhost-only` / `--yes`) and `crapi-dev.sh` (interactive menu wrapping the same operations). Both source `scripts/lib/platform.sh`, which detects macOS vs Linux and makes sure a Docker engine answers before anything runs. On macOS it tries Colima, then Docker Desktop, then OrbStack, and offers a Homebrew install of Colima if none exists. On Linux it checks the daemon and socket permissions. The scripts run under macOS's bash 3.2 (`#!/usr/bin/env bash` finds it first), so keep them 3.2-compatible: no `${var,,}`, associative arrays or `mapfile`. Check with `/bin/bash -n` and `shellcheck -x`. Extra docs not in upstream: `docs/crAPI_Exploitation_CheatSheet.md`, `docs/crAPI-NotesAndChallenges.md`.

## `deploy/docker/docker-compose.yml` is tracked and secret-free

It was gitignored for a while (commit `b02418c`) because a real API key lived in it. The key now lives only in `deploy/docker/llm.env.local` (gitignored by `*.local`), which `crapi-chatbot` loads through `env_file: - path: ./llm.env.local, required: false`. That makes the file optional, so a fresh clone deploys without it. `required: false` needs Compose 2.24+, which `quickstart.sh`'s `MIN_COMPOSE_VER` enforces. `deploy/docker/llm.env.example` is the tracked template. **Never write a key into `docker-compose.yml`**: check `grep -nE 'sk-|xai-' deploy/docker/docker-compose.yml` before committing it.

The file diverges from upstream:

- **`crapi-chatbot` is built from `../../services/chatbot`**, not pulled, and has `deploy.resources.limits.memory: 1G` (it OOM-kills without one). Its LLM settings (`LLM_PROVIDER`, `DEFAULT_MODEL`, `LLM_BASE_URL`, `EMBEDDINGS_PROVIDER`, `CHATBOT_LLM_API_KEY`) come only from `llm.env.local`. Values there must be unquoted, because literal quotes become part of the key. Upstream's `CHATBOT_OPENAI_API_KEY=${CHATBOT_OPENAI_API_KEY:-}` stays in `environment:` as a shell-export fallback. See README "Chatbot LLM Setup".
- **`crapi-web` publishes 8889, not 8888** (`"${LISTEN_IP:-127.0.0.1}:8889:80"`), because another app owns host port 8888. `quickstart.sh` and `crapi-dev.sh` hardcode `localhost:8889` for their health checks and printed URLs, so the compose port and the scripts must change together — a mismatch makes `quickstart.sh` end with "Some services are still starting" while the site is up. Host port 8080 is Burp Suite's proxy listener; don't publish anything there.
- **`deploy/docker/nginx-chatbot-disabled.ssl.conf.template`** is a fallback, not normally mounted. It is the stock SSL template with both `/chatbot/` blocks replaced by `return 503`. The stock template makes nginx refuse to start (`host not found in upstream crapi-chatbot`) whenever the chatbot container doesn't exist, so when the chatbot can't run, mount the stub over `/etc/nginx/conf.d/default.ssl.conf.template` on `crapi-web`. A commented-out `volumes:` entry for it sits under `crapi-web`. Leaving it mounted while the chatbot *is* running makes every `/chatbot/` request return 503 without reaching the container.

`deploy/docker/.env` holds only the non-secret toggles. Validate any compose edit with `docker compose config --quiet` from `deploy/docker/`. Plain `docker compose config` prints the resolved `llm.env.local` values, key included.

`quickstart.sh` deliberately pulls every service *except* `crapi-chatbot` and builds that one locally, so the fork's chatbot fixes are what runs.

## Running tests

Identity and Workshop tests need live databases. Start them first (this is a separate compose file from the app stack):

```bash
docker compose -f services/docker-database.yml up -d --wait
```

```bash
# Identity (Java) — all tests, then a single class
cd services/identity && ./gradlew test
./gradlew test --tests "com.crapi.service.Impl.UserServiceImplTest"

# Community (Go) — only api/controllers has tests today
cd services/community && go test -v ./...
go test -v ./api/controllers/ -run TestGetHome

# Workshop (Django) — note both the env sourcing and IS_TESTING
cd services/workshop && source venv/bin/activate && source .env
IS_TESTING=True python3 manage.py test --no-input
IS_TESTING=True python3 manage.py test crapi.shop.tests
IS_TESTING=True coverage run ./manage.py test --no-input crapi && coverage report
```

`services/workshop/.env` points at `127.0.0.1` for Postgres/Mongo, matching the ports `docker-database.yml` publishes. Workshop also needs system libs for Cairo (`libcairo2-dev pkg-config python3-dev` on Debian; `brew install cairo pkg-config` on macOS) plus `dev-requirements.txt` for the test runner.

The web service has no test files — `npm run lint` (prettier check) is its only CI gate. CI (`.github/workflows/pr-build.yml`) also runs `golangci-lint` v2.2.1 against `services/community`, builds every image, brings the stack up, and runs `newman run ./postman_collections/crAPI.postman_collection.json -e ./postman_collections/crAPI.postman_environment.json` as the end-to-end check. Running that Newman collection locally is the fastest way to confirm a cross-service change didn't break the stack.

## Architecture notes that span files

**Trust boundary.** Only `crapi-web` (8889 locally, 8888 upstream; 8443 for HTTPS; 30080/30443 as alternates), `crapi-identity` (8090), and `mailhog` (8025) are published to the host; everything else is reachable only on the internal Docker network. `crapi-web` is OpenResty, and `services/web/nginx.conf.template` + `nginx.ssl.conf.template` (templates rendered at container start, one block per protocol) define the real API surface: `/identity/`, `/community/`, `/workshop/`, `/chatbot/`, plus `/.well-known/jwks.json` proxied to the identity service's JWKS endpoint and `/mailhog/`. Adding a backend route means editing *both* templates, not just the service.

**Auth.** Identity (Spring Boot, `services/identity/src/main/java/com/crapi/`) is the only JWT issuer. Workshop and Community do not verify signatures locally — they POST to Identity's `/identity/api/auth/verify` over the internal network, addressed by the `IDENTITY_SERVICE` env var. A change to token claims or the JWKS in `services/identity/jwks.json` therefore propagates to all services at runtime, not at build time, and several JWT challenges depend on this indirection.

**Chatbot is a two-process container.** `services/chatbot/entrypoint.sh` starts `python -m mcpserver.server` in the background and then `uvicorn chatbot.app:app` in the foreground. The MCP server (`src/mcpserver/server.py`) builds its tool set by calling `FastMCP.from_openapi()` on `src/resources/crapi-openapi-spec.json` — so crAPI's own REST API is exposed to the LLM as MCP tools, authenticated with the hardcoded `admin@example.com` / `Admin!123` from `src/mcpserver/config.py`. The LangGraph agent (`src/chatbot/langgraph_agent.py`) reaches it over `http://localhost:5500/mcp/` (`src/chatbot/mcp_client.py`). This privileged-tool-behind-a-prompt design *is* the LLM challenge set (prompt injection, credential extraction, action hijacking): the agent runs as admin regardless of who is chatting. An LLM key is required. `config.py` reads `CHATBOT_LLM_API_KEY`, then upstream's `CHATBOT_OPENAI_API_KEY`. The chat model is provider-agnostic: `build_llm()` in `langgraph_agent.py` calls LangChain's `init_chat_model()` with `LLM_PROVIDER` (`openai`, which also covers any OpenAI-compatible API via `LLM_BASE_URL`, or `anthropic`; other providers need their `langchain-*` package). `langchain-anthropic` is pinned to 1.0.1 because newer releases force `langchain-core` past the pinned 1.0.2. `EMBEDDINGS_PROVIDER` defaults to `openai` only for plain OpenAI and to `local` otherwise; local embeddings use Chroma's ONNX MiniLM, baked into the image, in a separate `chats_local` collection; `src/mcpserver/tool_helpers.py` patches the spec (ref resolution, array responses) because FastMCP can't consume it raw. Both processes import `chatbot.extensions`, which connects to Postgres at import time, so a broken DB driver kills the whole container: `POSTGRES_URI` in `src/chatbot/dbconnections.py` must stay `postgresql+psycopg2://` — SQLAlchemy is unpinned, and 2.1+ resolves a bare `postgresql://` to psycopg v3, which the image doesn't install.

**SSRF target.** `services/gateway-service` (Go, HTTPS-only, `api.mypremiumdealership.com`) fabricates deterministic PII with `jaswdr/faker` seeded from the input, and is intentionally reachable from Workshop — that reachability is the SSRF challenge, not an oversight.

**Frontend data flow.** `services/web/src/` is React 18 + TypeScript with Redux and Redux-Saga; every API call lives in `src/sagas/`, so that directory is the index of what the frontend actually hits. `src/config.ts.template` is rendered at container start.

**Persistence split.** Postgres backs identity/community/workshop; Mongo backs community, workshop, chatbot, and mailhog; ChromaDB is the chatbot's RAG vector store. All `@example.com` mail is trapped by Mailhog at :8025 regardless of SMTP config — OTP and password-reset flows are read there.
