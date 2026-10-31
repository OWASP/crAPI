# crAPI

[![pipeline status](https://gitlab.com/LinuxUser255/crAPI/badges/main/pipeline.svg)](https://gitlab.com/LinuxUser255/crAPI/-/pipelines?ref=main)
[![latest release](https://gitlab.com/LinuxUser255/crAPI/-/badges/release.svg)](https://gitlab.com/LinuxUser255/crAPI/-/releases)

**c**ompletely **r**idiculous **API** (crAPI) will help you to understand the
ten most critical API security risks. crAPI is vulnerable by design, but you'll
be able to safely run it to educate/train yourself.

crAPI is modern, built on top of a microservices architecture. When time has
come to buy your first car, sign up for an account and start your journey. To
know more about crAPI, please check [crAPI's overview][overview].

This is a fork of [OWASP/crAPI](https://github.com/OWASP/crAPI), set up to run on macOS with Colima. It adds a one-command quick start, a chatbot that works with any LLM provider (OpenAI, Anthropic, xAI, and other OpenAI-compatible APIs), and its own port layout.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Chatbot LLM Setup](#chatbot-llm-setup)
   - [The settings](#the-settings)
   - [Supported providers](#supported-providers)
   - [Step 1: Get an API key](#step-1-get-an-api-key)
   - [Step 2: Create llm.env.local](#step-2-create-llmenvlocal)
   - [Step 3: Start or restart the chatbot](#step-3-start-or-restart-the-chatbot)
   - [Step 4: Verify the chatbot's settings](#step-4-verify-the-chatbots-settings)
   - [Chatbot troubleshooting](#chatbot-troubleshooting)
   - [Switching providers, rotating, and revoking](#switching-providers-rotating-and-revoking)
3. [Local Environment](#local-environment)
   - [Docker runtime on macOS and Linux](#docker-runtime-on-macos-and-linux)
   - [Ports](#ports)
   - [Proxying through Burp Suite](#proxying-through-burp-suite)
   - [Troubleshooting the local setup](#troubleshooting-the-local-setup)
4. [Other Ways to Run crAPI](#other-ways-to-run-crapi)
   - [Docker Compose with prebuilt images](#docker-compose-with-prebuilt-images)
   - [Vagrant](#vagrant)
   - [Kubernetes and other options](#kubernetes-and-other-options)
5. [Challenges and Documentation](#challenges-and-documentation)
6. [Development Workflow and Releases](#development-workflow-and-releases)
7. [Getting Help](#getting-help)

---

## Quick Start

Run [`quickstart.sh`](quickstart.sh) from the repo root:

```bash
./quickstart.sh                    # binds to all interfaces (reachable from your LAN)
./quickstart.sh --localhost-only   # binds to 127.0.0.1 only
./quickstart.sh --vulnerable       # enable all vulnerabilities (shell injection, log4j)
./quickstart.sh --yes              # don't ask before installing or starting Docker
```

The same command works on macOS and Linux. The script detects the platform and gets Docker running before it deploys. On macOS that means Colima (see [Docker runtime on macOS and Linux](#docker-runtime-on-macos-and-linux)).

Optional, for the AI chatbot: before the first run, copy `deploy/docker/llm.env.example` to `deploy/docker/llm.env.local` and add your LLM provider and key. See [Chatbot LLM Setup](#chatbot-llm-setup). Without it everything else works, and the chatbot reports `Model needs to be initialized`.

From a fresh clone that is the whole setup:

```bash
git clone https://gitlab.com/LinuxUser255/crAPI.git && cd crAPI
cp deploy/docker/llm.env.example deploy/docker/llm.env.local   # optional: then add your key
./quickstart.sh
```

`main` is always deployable. To pin a tested version instead, clone a release tag, e.g. `git clone --branch v1.0.0 https://gitlab.com/LinuxUser255/crAPI.git`. All versions and their notes are on the [releases page](https://gitlab.com/LinuxUser255/crAPI/-/releases) and in [`CHANGELOG.md`](CHANGELOG.md).

`quickstart.sh` pulls the upstream images and builds `crapi-chatbot`, `crapi-community` and `crapi-web` from local source, since those carry this fork's changes. When it finishes, open [http://localhost:8889](http://localhost:8889). [`crapi-dev.sh`](crapi-dev.sh) wraps the same operations in an interactive menu.

[↑ Back to contents](#table-of-contents)

---

## Chatbot LLM Setup

The chatbot is provider-agnostic. It works with OpenAI, Anthropic, and any API that speaks the OpenAI protocol: xAI (Grok), Google Gemini, Groq, Mistral, DeepSeek, OpenRouter, or a local Ollama. All of its LLM settings, key included, live in one file, `deploy/docker/llm.env.local`. Docker Compose loads that file into the `crapi-chatbot` container. It is gitignored (`*.local`), so the key never reaches git, while `docker-compose.yml` itself is tracked and contains no secrets.

For a diagram of how the key travels from that file to the container to the provider, and what keeps it hidden along the way, see the [Chatbot API Key Flow](docs/chatbot-api-key-flow.md).

### The settings

| Setting in `llm.env.local` | What it does | If left empty |
| --- | --- | --- |
| `LLM_PROVIDER` | Which client library to use: `openai` (OpenAI and every OpenAI-compatible API) or `anthropic` | `openai` |
| `DEFAULT_MODEL` | Model id, exactly as the provider names it. It must support tool calling | `gpt-4o-mini` |
| `LLM_BASE_URL` | The API endpoint. Set it for OpenAI-compatible providers | the provider's default |
| `EMBEDDINGS_PROVIDER` | Chat-history memory: `openai` (`text-embedding-3-large`) or `local` (`all-MiniLM-L6-v2`, built into the image, no key needed) | `openai` for plain OpenAI, `local` for everything else |
| `CHATBOT_LLM_API_KEY` | The provider's API key | no key: the chat window asks for one |

Upstream's `CHATBOT_OPENAI_API_KEY`, exported in your shell before `docker compose up`, still works as a fallback for the key.

### Supported providers

Copy the row for your provider. The model must support tool (function) calling, because the chatbot's agent runs on tools.

| Provider | `LLM_PROVIDER` | `LLM_BASE_URL` | Model (example) | Get a key |
| --- | --- | --- | --- | --- |
| OpenAI | `openai` | *(empty)* | `gpt-4o-mini` | [platform.openai.com](https://platform.openai.com/api-keys) |
| Anthropic (Claude) | `anthropic` | *(empty)* | `claude-sonnet-5-5` | [console.anthropic.com](https://console.anthropic.com/settings/keys) |
| xAI (Grok) | `openai` | `https://api.x.ai/v1` | `grok-4.3` | [console.x.ai](https://console.x.ai) |
| Google Gemini | `openai` | `https://generativelanguage.googleapis.com/v1beta/openai/` | from the provider's model list | [aistudio.google.com](https://aistudio.google.com/apikey) |
| Groq | `openai` | `https://api.groq.com/openai/v1` | from the provider's model list | [console.groq.com](https://console.groq.com/keys) |
| Mistral | `openai` | `https://api.mistral.ai/v1` | from the provider's model list | [console.mistral.ai](https://console.mistral.ai/api-keys) |
| DeepSeek | `openai` | `https://api.deepseek.com` | from the provider's model list | [platform.deepseek.com](https://platform.deepseek.com/api_keys) |
| OpenRouter (many vendors, one key) | `openai` | `https://openrouter.ai/api/v1` | from the provider's model list | [openrouter.ai](https://openrouter.ai/keys) |
| Ollama on your machine (no key, no cost) | `openai` | `http://host.docker.internal:11434/v1` | a model you've pulled, e.g. via `ollama pull` | not needed; set the key to any text, e.g. `ollama` |

Only OpenAI, Anthropic and xAI have been checked against this code; requests to each reached the provider. The others follow the same OpenAI-compatible path but are untested here. Leave `EMBEDDINGS_PROVIDER` empty unless you have a reason not to.

For Ollama under Colima, if `host.docker.internal` doesn't resolve from the container, use `host.lima.internal` instead.

Other providers LangChain supports natively (for example `google_genai`, `mistralai`, `bedrock`) work too. Add the matching `langchain-*` package to `services/chatbot/requirements.txt`, rebuild, and set `LLM_PROVIDER` to LangChain's provider name.

### Step 1: Get an API key

1. Create a key on your provider's site (the **Get a key** column above). Ollama needs none.
2. Copy it once and keep it in your password manager. Most providers won't show it again.
3. Optional: check that the key works and list the model ids it can use. `read -s` keeps the key out of your shell history and off the screen:

   ```bash
   read -s "LLM_KEY?API key: "    # zsh; in bash use: read -rsp "API key: " LLM_KEY

   # OpenAI or any OpenAI-compatible provider: use its base URL (OpenAI's is https://api.openai.com/v1)
   curl -s https://api.x.ai/v1/models -H "Authorization: Bearer $LLM_KEY" | grep -o '"id": *"[^"]*"'

   # Anthropic
   curl -s https://api.anthropic.com/v1/models -H "x-api-key: $LLM_KEY" -H "anthropic-version: 2023-06-01" | grep -o '"id": *"[^"]*"'

   unset LLM_KEY
   ```

   Pick one of the listed ids for `DEFAULT_MODEL`.

### Step 2: Create llm.env.local

1. From the repo root, copy the template and make it readable only by you:

   ```bash
   cp deploy/docker/llm.env.example deploy/docker/llm.env.local
   chmod 600 deploy/docker/llm.env.local
   ```

2. Confirm git ignores it. This should print a line naming `.gitignore` and `*.local`:

   ```bash
   git check-ignore -v deploy/docker/llm.env.local
   ```

   If it prints nothing, stop. The file isn't ignored, and a key put in it would be committed.

3. Open `deploy/docker/llm.env.local` in your editor and fill in your row from [Supported providers](#supported-providers). For example:

   ```bash
   # Anthropic
   LLM_PROVIDER=anthropic
   DEFAULT_MODEL=claude-sonnet-5-5
   LLM_BASE_URL=
   EMBEDDINGS_PROVIDER=
   CHATBOT_LLM_API_KEY=sk-ant-your-key-here
   ```

   ```bash
   # xAI, or any other OpenAI-compatible provider (swap in its base URL and model)
   LLM_PROVIDER=openai
   DEFAULT_MODEL=grok-4.3
   LLM_BASE_URL=https://api.x.ai/v1
   EMBEDDINGS_PROVIDER=
   CHATBOT_LLM_API_KEY=xai-your-key-here
   ```

   Rules for the file:
   - One `NAME=value` per line, with no `export` in front and **no quotes** around values.
   - Keep the names exactly as shown. Provider-specific names such as `XAI_API_KEY` or `ANTHROPIC_API_KEY` reach the container, but the chatbot doesn't read them.
   - Only the key goes after `CHATBOT_LLM_API_KEY=`. A value like `CHATBOT_LLM_API_KEY=XAI_API_KEY=xai-...` sends the text `XAI_API_KEY=xai-...` as the key.

Never put a key in these **tracked** files instead:

- `deploy/docker/docker-compose.yml` or `deploy/docker/.env`
- `deploy/docker/llm.env.example`, the template
- `services/chatbot/.env`: this file is only for running the chatbot outside Docker. Docker never reads it. `.gitignore` doesn't protect it either, because git already tracks it.

### Step 3: Start or restart the chatbot

- **Fresh start:** run `./quickstart.sh` from the repo root. It reports whether it found `llm.env.local`.
- **Stack already running:** recreate only the chatbot. From `deploy/docker`:

  ```bash
  docker compose up -d --no-deps crapi-chatbot
  ```

  After pulling new chatbot code, run `docker compose build crapi-chatbot` first.

A changed setting reaches the container only when it is recreated. `docker compose restart` does **not** re-read `llm.env.local`, so use `up -d` instead.

### Step 4: Verify the chatbot's settings

These commands show the key's prefix and length, never the key itself:

```bash
docker exec crapi-chatbot sh -c 'echo "prefix=${CHATBOT_LLM_API_KEY%%-*} length=${#CHATBOT_LLM_API_KEY}"'
# expect your provider's prefix (sk, xai, gsk, ...) and a non-zero length

docker exec crapi-chatbot printenv LLM_PROVIDER LLM_BASE_URL DEFAULT_MODEL EMBEDDINGS_PROVIDER
# expect the values from your llm.env.local, one per line. An empty value prints as a blank line.

docker logs --tail 50 crapi-chatbot
```

Avoid plain `docker compose config`. It prints the resolved file, **key included**. Use `docker compose config --quiet` to validate.

Then open crAPI at [http://localhost:8889](http://localhost:8889), log in, open the chat window, and ask it something like "What endpoints does crAPI expose?". A real answer means the key, provider, and model all work. The chat window's own key prompt says "OpenAI API key", because it comes from the prebuilt `crapi-web` image. It only appears when the container has no key, and it accepts a key for whichever provider is configured.

### Chatbot troubleshooting

| Symptom | Cause and fix |
| --- | --- |
| UI says `Model needs to be initialized`, or the API returns `Missing OpenAI API key` | The container has no key. Check that `deploy/docker/llm.env.local` exists and sets `CHATBOT_LLM_API_KEY`, then recreate the container ([Step 3](#step-3-start-or-restart-the-chatbot)) and run the [Step 4](#step-4-verify-the-chatbots-settings) check. |
| `401`, `authentication_error`, or `Incorrect API key` in `docker logs crapi-chatbot` | Quotes around the key, a stray `NAME=` prefix in the value, or a key sent to the wrong provider. Check that `LLM_PROVIDER` and `LLM_BASE_URL` match the key's provider. For example, an xAI key with an empty base URL goes to OpenAI. |
| `404`, `model not found`, or `not_found_error` | `DEFAULT_MODEL` isn't an id your key can use. List the models ([Step 1](#step-1-get-an-api-key), item 3) and set one of them. |
| `Unsupported model_provider` or `Unable to import langchain_...` | `LLM_PROVIDER` is misspelled, or names a provider whose `langchain-*` package isn't installed. Use `openai` or `anthropic`, or add the package (see [Supported providers](#supported-providers)). |
| Errors mentioning embeddings or `text-embedding-3-large` | `EMBEDDINGS_PROVIDER` is `openai` with a non-OpenAI key. Leave it empty or set it to `local`, then recreate the container. |
| The model answers but never calls tools, or tool calls fail | The model doesn't support tool calling. Pick one that does. |
| `quickstart.sh` says Compose is too old | `env_file` with `required: false`, which keeps `llm.env.local` optional, needs Docker Compose 2.24 or later. |
| Changes don't take effect | You ran `restart`, or skipped the rebuild after pulling new code. See [Step 3](#step-3-start-or-restart-the-chatbot). |
| Every `/chatbot/` request returns `503 crapi-chatbot is not running` | See [Troubleshooting the local setup](#troubleshooting-the-local-setup). |

### Switching providers, rotating, and revoking

- **Switching providers:** change all five lines in `llm.env.local` together, using your row from [Supported providers](#supported-providers), then recreate the container ([Step 3](#step-3-start-or-restart-the-chatbot)). To switch back and forth, keep one file per provider (`anthropic.env.local`, `xai.env.local`, …) and copy the one you want over `llm.env.local`. Chat memory built with one embedding model (the `chats` collection for `openai`, `chats_local` for `local`) isn't visible under the other.
- **Rotating:** create a new key with your provider, replace the value in `llm.env.local`, recreate the container, then delete the old key in the provider's console.
- **If a key was ever committed or pasted somewhere public:** revoke it with the provider right away. Deleting it in a later commit doesn't remove it from git history.
- Anyone who can run `docker inspect crapi-chatbot` on this machine can read the key. The chatbot's admin-level agent is a deliberate prompt-injection target, so use a key with a spending limit.

[↑ Back to contents](#table-of-contents)

---

## Local Environment

### Docker runtime on macOS and Linux

`quickstart.sh` and `crapi-dev.sh` both call [`scripts/lib/platform.sh`](scripts/lib/platform.sh), which detects the OS and makes sure a Docker engine is answering before anything else runs. You don't need to start Docker by hand on either platform.

**macOS** has no native Docker engine; it runs inside a Linux VM. The script checks, in order:

1. **Docker already answering** (any runtime): use it.
2. **Colima installed**: start it. An existing VM keeps its settings from `~/.colima/default/colima.yaml`. A new VM is created with 4 CPUs and 8 GiB, because Colima's default of 2 CPUs and 2 GiB is too small for crAPI (the chatbot alone has a 1 GiB limit).
3. **Docker Desktop or OrbStack installed**: open it and wait for its engine.
4. **Nothing installed**: offer to run `brew install colima docker docker-compose`, then start Colima.

**Linux**: if the Docker daemon isn't running, the script offers to start it with `sudo systemctl start docker` (or `service docker start`). If your user can't reach the Docker socket, it prints the `usermod -aG docker` fix. If Docker isn't installed, it prints your package manager's install command next to the existing build-from-source steps.

On both platforms it warns if Docker has less than about 6 GiB of memory, and tells you how to raise it for your runtime.

Settings, as environment variables:

| Variable | Default | Effect |
| --- | --- | --- |
| `COLIMA_CPU` | `4` | CPUs for a **new** Colima VM |
| `COLIMA_MEMORY` | `8` | GiB of memory for a **new** Colima VM |
| `COLIMA_PROFILE` | `default` | Which Colima profile to start |
| `DOCKER_WAIT_SECS` | `120` | How long to wait for the engine to answer |

Installs and `sudo` always ask first. Pass `--yes` to `quickstart.sh` to answer yes in advance, which you need when no terminal is attached (CI, a pipe). Starting an already-installed Colima, Docker Desktop or OrbStack doesn't ask.

To resize an existing Colima VM, which the script never changes on its own:

```bash
colima stop && colima start --cpu 4 --memory 8
```

Colima doesn't start on its own after a reboot. The script starts it for you, or `brew services start colima` starts it at login.

### Ports

This fork's `docker-compose.yml` publishes the web UI on **8889** instead of upstream's 8888, because 8888 is used by another app on the host. `quickstart.sh` and `crapi-dev.sh` health-check and print 8889 to match.

| URL | What |
| --- | --- |
| [https://localhost:8443](https://localhost:8443) | crAPI web UI (HTTPS) |
| [http://localhost:8889](http://localhost:8889) | crAPI web UI (HTTP) |
| [http://localhost:8025](http://localhost:8025) | Mailhog (signup OTPs, password resets) |
| [https://localhost:8443/mailhog/](https://localhost:8443/mailhog/) | Mailhog through the web front end |
| `localhost:8090` | Identity service (direct) |
| `30080` / `30443` | Alternate HTTP / HTTPS ports for the web UI |

Everything else (community, workshop, chatbot, databases, `api.mypremiumdealership.com`) is reachable only on the internal Docker network.

All emails to `@example.com` addresses go to Mailhog, whatever the SMTP configuration says.

### Proxying through Burp Suite

Host port **8080** is left free for Burp Suite's proxy listener. Point your browser at `127.0.0.1:8080` and browse crAPI as usual. If `localhost` traffic bypasses the proxy, browse to the host's LAN IP instead.

### Troubleshooting the local setup

- **`failed to connect to the docker API at unix:///Users/<you>/.colima/default/docker.sock`** — Colima's VM is stopped. Rerun `quickstart.sh`, which starts it, or run `colima start`.
- **`crapi-chatbot` exits with `ModuleNotFoundError: No module named 'psycopg'`** — SQLAlchemy 2.1 made psycopg v3 the default driver for `postgresql://` URIs, but the image ships `psycopg2-binary`. Fixed by using `postgresql+psycopg2://` in `services/chatbot/src/chatbot/dbconnections.py`; rebuild with `docker compose build crapi-chatbot && docker compose up -d --no-deps crapi-chatbot` from `deploy/docker`.
- **Every `/chatbot/` request returns `503 crapi-chatbot is not running`** — `crapi-web` is mounting `deploy/docker/nginx-chatbot-disabled.ssl.conf.template`, a stub for when the chatbot can't run (the stock template makes nginx refuse to start if the `crapi-chatbot` host doesn't exist). Remove that volume from `crapi-web` in `docker-compose.yml` once the chatbot is up, then `docker compose up -d --no-deps crapi-web`.
- **`quickstart.sh` ends with "Some services are still starting" even though the site is up** — its health check is hitting a different port than the web UI is published on. Make sure the port in `docker-compose.yml` matches the one in the scripts (8889).
- **Chatbot key problems** — see [Chatbot troubleshooting](#chatbot-troubleshooting).

[↑ Back to contents](#table-of-contents)

---

## Other Ways to Run crAPI

### Docker Compose with prebuilt images

You need Docker and docker compose `1.27.0` or later. Check with:

```bash
docker compose version
```

If you see `ERROR: Invalid interpolation format for ...`, upgrade docker compose.

These steps download upstream's compose and **.env** files and run the images built by upstream CI. Pick a version:

| Version | Branch | Archive URL | Folder after unzip |
| --- | --- | --- | --- |
| Latest stable | `main` | `https://github.com/OWASP/crAPI/archive/refs/heads/main.zip` | `crAPI-main` |
| Latest development | `develop` | `https://github.com/OWASP/crAPI/archive/refs/heads/develop.zip` | `crAPI-develop` |

**Linux or macOS** (stable shown; for development, swap `main` for `develop`):

```bash
curl -L -o /tmp/crapi.zip https://github.com/OWASP/crAPI/archive/refs/heads/main.zip
unzip /tmp/crapi.zip
cd crAPI-main/deploy/docker
docker compose pull
docker compose -f docker-compose.yml --compatibility up -d
```

**Windows** (stable shown; for development, swap `main` for `develop`):

```powershell
curl.exe -L -o crapi.zip https://github.com/OWASP/crAPI/archive/refs/heads/main.zip
tar -xf .\crapi.zip
cd crAPI-main/deploy/docker
docker compose pull
docker compose -f docker-compose.yml --compatibility up -d
```

To override server settings, change the variables in the **.env** file, or set them in front of the command. For example, to expose crAPI on all network interfaces:

```bash
LISTEN_IP="0.0.0.0" docker compose -f docker-compose.yml --compatibility up -d
```

Then visit [http://localhost:8888](http://localhost:8888). Upstream's compose file uses port 8888, unlike this fork's 8889. Emails go to Mailhog at [http://localhost:8025](http://localhost:8025). You can change the SMTP configuration, but all emails to the **example.com** domain still go to Mailhog.

### Vagrant

This option runs crAPI in a virtual machine, isolated from your system. You need [Vagrant] and a provider such as [VirtualBox].

1. Clone the crAPI repository:
   ```bash
   git clone [REPOSITORY-URL]
   ```
2. Start the crAPI virtual machine:
   ```bash
   cd deploy/vagrant && vagrant up
   ```
3. Visit [http://192.168.33.20](http://192.168.33.20). Emails go to Mailhog at [http://192.168.33.20:8025](http://192.168.33.20:8025).

When you're done, remove the VM by running this from the repository root:

```bash
cd deploy/vagrant && vagrant destroy
```

### Kubernetes and other options

See [the setup instructions](docs/setup.md), including [Kubernetes][setup-k8s], and [teardown](docs/teardown.md) for removing a deployment.

[↑ Back to contents](#table-of-contents)

---

## Challenges and Documentation

| Document | What's in it |
| --- | --- |
| [Overview][overview] | What crAPI is and how it's built |
| [Architecture](docs/crAPI_architecture.md) | How crAPI is modelled as an application and how its parts fit together |
| [Challenges][challenges] | The OWASP API Top 10 challenge list |
| [Challenge solutions](docs/challengeSolutions.md) | Walkthroughs for each challenge |
| [Exploitation cheat sheet](docs/crAPI_Exploitation_CheatSheet.md) | Quick reference for attacking crAPI (this fork) |
| [Notes and challenges](docs/crAPI-NotesAndChallenges.md) | Exploitation examples and lesson links (this fork) |
| [Chatbot API Key Flow](docs/chatbot-api-key-flow.md) | Flowchart of how the chatbot's LLM key (any provider) is sourced and kept out of git (this fork) |
| [Setup](docs/setup.md) / [Teardown](docs/teardown.md) | All deployment options |
| [Troubleshooting](docs/troubleshooting.md) | General install and runtime issues |

[↑ Back to contents](#table-of-contents)

---

## Development Workflow and Releases

| Branch | Purpose |
| --- | --- |
| `main` | Protected and always deployable. Changes arrive only through merge requests from `develop`. Releases are tagged here. |
| `develop` | Where all changes are made and pushed. |

1. **Make changes on `develop`** and push. GitLab CI runs on every push ([`.gitlab-ci.yml`](.gitlab-ci.yml)):

   | Job | Checks |
   | --- | --- |
   | `shellcheck` | `quickstart.sh`, `scripts/lib/platform.sh` (all warnings), `crapi-dev.sh` (errors) |
   | `bash-3.2-syntax` | The scripts parse under bash 3.2, the bash a stock Mac runs |
   | `compose-config` | `docker-compose.yml` validates with no `llm.env.local`, as in a fresh clone |
   | `chatbot-python` | The chatbot source compiles |
   | `secret_detection` | GitLab's gitleaks scan for committed keys. GitLab's template runs it in branch pipelines only: `main`, and `develop` while no merge request is open. It skips merge request and tag pipelines; a release tag always points at a `main` commit that was already scanned |
   | `chatbot-image` | Builds the chatbot image and checks both LLM client libraries and the offline embedding model. Runs on merge requests, `main` and tags |

2. **Open a merge request from `develop` to `main`.** From the command line:

   ```bash
   git push -o merge_request.create -o merge_request.target=main origin develop
   ```

   Merge it once its pipeline passes.

3. **Release** from an up-to-date `main`. First add a `## [vX.Y.Z] - YYYY-MM-DD` section at the top of [`CHANGELOG.md`](CHANGELOG.md) (on `develop`, merged like any change). Then:

   ```bash
   git switch main && git pull
   git tag -a vX.Y.Z -m "crAPI lab vX.Y.Z"
   git push origin vX.Y.Z
   ```

   The tag pipeline's `release` job creates the GitLab release, with that version's `CHANGELOG.md` section as its notes. Releases are listed under **Deploy > Releases** in GitLab.

[↑ Back to contents](#table-of-contents)

---

## Getting Help

For issues specific to this fork's setup, start with [Troubleshooting the local setup](#troubleshooting-the-local-setup) and [Chatbot troubleshooting](#chatbot-troubleshooting). For general install and runtime problems, see the upstream [troubleshooting guide](https://github.com/OWASP/crAPI/blob/main/docs/troubleshooting.md). If that doesn't solve it, open an issue on GitHub.

[↑ Back to contents](#table-of-contents)

[challenges]: docs/challenges.md
[overview]: docs/overview.md
[setup-k8s]: docs/setup.md#kubernetes
[vagrant]: https://www.vagrantup.com/downloads
[virtualbox]: https://www.virtualbox.org/wiki/Downloads
