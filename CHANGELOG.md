# Changelog

Releases of this crAPI lab fork. Each `## [vX.Y.Z]` section becomes the notes of the GitLab release for that tag (see `.gitlab-ci.yml`). Add new versions at the top.

## [Unreleased]

### Forum

- Posts can be liked and disliked, from the forum list and the post page. Each is a toggle per user: reacting again removes it, and liking a disliked post (or the reverse) switches it. New Community endpoints: `POST /community/api/v2/community/posts/{id}/like` and `/dislike`; posts now carry `likes`, `dislikes` and `user_reaction`. The chatbot's OpenAPI spec knows the new routes.
- Forum posts and comments show the author's current profile picture. Before, the picture was copied into the post when it was written, so one uploaded afterwards never appeared.

### Chatbot

- The chat window can be resized by dragging its top edge, left edge or top-left corner. The size is remembered in the browser.

### Docs

- Header comments on the main files of every backend service describing what each does and what it connects to, and `docs/crAPI_architecture_detailed.md`, a drill-down of the architecture overview that separates frontend from backend.

## [v1.0.0] - 2026-10-30

First release of the fork as a ready-to-run lab, based on [OWASP/crAPI](https://github.com/OWASP/crAPI) `develop`.

### One-command setup

- `./quickstart.sh` deploys the whole lab on macOS or Linux. It detects the platform and gets a Docker engine running first: Colima, Docker Desktop or OrbStack on macOS (offering a Homebrew install of Colima if none exists), and the system daemon on Linux. Pass `--yes` for runs without a terminal.
- `docker-compose.yml` is tracked again and contains no secrets, so a fresh clone deploys as is.
- `crapi-dev.sh` offers the same operations as an interactive menu.

### Chatbot works with any LLM provider

- The chatbot works with OpenAI, Anthropic, and any OpenAI-compatible API (xAI, Gemini, Groq, Mistral, DeepSeek, OpenRouter, Ollama), chosen with `LLM_PROVIDER`, `LLM_BASE_URL` and `DEFAULT_MODEL`.
- All LLM settings and the key live in `deploy/docker/llm.env.local`, which is gitignored and optional. Copy `deploy/docker/llm.env.example` to create it.
- Chat-history embeddings can run locally (`all-MiniLM-L6-v2`, built into the image) for providers without an embeddings API.

### Fixes over upstream

- The chatbot is built from local source with a 1 GiB memory limit, and its Postgres driver is pinned to `psycopg2`.
- The web UI is published on 8889 instead of 8888, and host port 8080 is left free for Burp Suite.
- A fallback nginx config lets the web UI start when the chatbot can't run.

### Docs and CI

- The README has a linked table of contents and step-by-step chatbot setup. A flowchart of how the API key is handled is in `docs/chatbot-api-key-flow.md`.
- The GitLab CI pipeline runs shellcheck, a bash 3.2 syntax check, compose validation, a Python syntax check, secret detection, and a chatbot image build and smoke test. A `vX.Y.Z` tag on `main` creates a release.
