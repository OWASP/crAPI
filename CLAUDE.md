# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

`AGENTS.md` (also auto-loaded) covers the stack-level basics: compose lifecycle commands, per-service build/lint commands, service port map, and the vulnerability toggles in `deploy/docker/.env`. This file covers what that leaves out — local-setup gotchas, test invocation, and the cross-file architecture.

## This is an intentionally vulnerable app

crAPI exists to be exploited (OWASP API Top 10 training). Vulnerabilities are features. Never "harden" code unless explicitly asked — removing a BOLA check or an unsanitized query breaks a documented challenge in `docs/challenges.md`.

This is a fork of OWASP/crAPI (`develop` is the main branch here; local work happens on `macos`). Fork-specific additions at the repo root: `quickstart.sh` (non-interactive deploy, `--vulnerable` / `--localhost-only`) and `crapi-dev.sh` (interactive menu wrapping the same operations). Extra docs not in upstream: `docs/crAPI_Exploitation_CheatSheet.md`, `docs/crAPI-NotesAndChallenges.md`.

## `deploy/docker/docker-compose.yml` is gitignored

It was removed from tracking in commit `b02418c` and added to `.gitignore` so that a real `CHATBOT_OPENAI_API_KEY` never lands in git. A local copy exists in the working tree and is what every compose command in `AGENTS.md`, `README.md`, and `quickstart.sh` depends on — if it ever goes missing, rebuild it with:

```bash
git show b02418c^:deploy/docker/docker-compose.yml > deploy/docker/docker-compose.yml
```

then re-apply the three compose-side chatbot fixes from `b02418c` (the restored version predates them): a `build: context: ../../services/chatbot` on `crapi-chatbot` so the local source is built rather than pulled, a `deploy.resources.limits.memory: 1G` on that service (it OOM-kills without one), and an unquoted `CHATBOT_OPENAI_API_KEY` — literal quotes break the OpenAI client. The current file reads that key from the environment (`${CHATBOT_OPENAI_API_KEY:-}`), so export it in your shell before `docker compose up` or set the value inline; it resolves to empty otherwise and the chatbot's LLM features stay dead.

`deploy/docker/.env` *is* tracked and holds only the non-secret toggles. Validate any edit with `docker compose config --quiet` from `deploy/docker/`.

`quickstart.sh` deliberately pulls every service *except* `crapi-chatbot` and builds that one locally, for the same reason.

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

**Trust boundary.** Only `crapi-web` (8888/8443) and `crapi-identity` (8090) are published to the host; everything else is reachable only on the internal Docker network. `crapi-web` is OpenResty, and `services/web/nginx.conf.template` + `nginx.ssl.conf.template` (templates rendered at container start, one block per protocol) define the real API surface: `/identity/`, `/community/`, `/workshop/`, `/chatbot/`, plus `/.well-known/jwks.json` proxied to the identity service's JWKS endpoint and `/mailhog/`. Adding a backend route means editing *both* templates, not just the service.

**Auth.** Identity (Spring Boot, `services/identity/src/main/java/com/crapi/`) is the only JWT issuer. Workshop and Community do not verify signatures locally — they POST to Identity's `/identity/api/auth/verify` over the internal network, addressed by the `IDENTITY_SERVICE` env var. A change to token claims or the JWKS in `services/identity/jwks.json` therefore propagates to all services at runtime, not at build time, and several JWT challenges depend on this indirection.

**Chatbot is a two-process container.** `services/chatbot/entrypoint.sh` starts `python -m mcpserver.server` in the background and then `uvicorn chatbot.app:app` in the foreground. The MCP server (`src/mcpserver/server.py`) builds its tool set by calling `FastMCP.from_openapi()` on `src/resources/crapi-openapi-spec.json` — so crAPI's own REST API is exposed to the LLM as MCP tools, authenticated with the hardcoded `admin@example.com` / `Admin!123` from `src/mcpserver/config.py`. The LangGraph agent (`src/chatbot/langgraph_agent.py`) reaches it over `http://localhost:5500/mcp/` (`src/chatbot/mcp_client.py`). This privileged-tool-behind-a-prompt design *is* the LLM challenge set (prompt injection, credential extraction, action hijacking): the agent runs as admin regardless of who is chatting. `CHATBOT_OPENAI_API_KEY` is required; `src/mcpserver/tool_helpers.py` patches the spec (ref resolution, array responses) because FastMCP can't consume it raw.

**SSRF target.** `services/gateway-service` (Go, HTTPS-only, `api.mypremiumdealership.com`) fabricates deterministic PII with `jaswdr/faker` seeded from the input, and is intentionally reachable from Workshop — that reachability is the SSRF challenge, not an oversight.

**Frontend data flow.** `services/web/src/` is React 18 + TypeScript with Redux and Redux-Saga; every API call lives in `src/sagas/`, so that directory is the index of what the frontend actually hits. `src/config.ts.template` is rendered at container start.

**Persistence split.** Postgres backs identity/community/workshop; Mongo backs community, workshop, chatbot, and mailhog; ChromaDB is the chatbot's RAG vector store. All `@example.com` mail is trapped by Mailhog at :8025 regardless of SMTP config — OTP and password-reset flows are read there.
