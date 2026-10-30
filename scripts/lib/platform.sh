#!/usr/bin/env bash
#==============================================================================
# FILE:         scripts/lib/platform.sh
# DESCRIPTION:  Platform detection and Docker runtime bootstrap, shared by
#               quickstart.sh and crapi-dev.sh. Sourced, never executed.
#
#               macOS has no native Docker engine, so a VM runtime must be
#               running before any docker command works. This library finds one
#               and starts it: Colima first, then Docker Desktop, then OrbStack.
#               If none is installed it offers to install Colima with Homebrew.
#               On Linux it checks the Docker daemon and socket permissions and
#               offers to start the daemon through systemd or service.
#
#               Bash 3.2 compatible, because that is macOS's /bin/bash and the
#               bash `env` finds first on a stock Mac: no ${var,,}, no
#               associative arrays, no mapfile.
#
# PUBLIC:       detect_platform        sets PLATFORM_OS, PLATFORM_NAME,
#                                      PLATFORM_ARCH, PKG_MANAGER
#               ensure_docker_runtime  returns once `docker info` works, or exits
#               print_compose_install_hint
#               print_docker_install_hint
#
# ENV:          ASSUME_YES        true answers yes to install/start prompts
#               COLIMA_PROFILE    Colima profile to use (default: default)
#               COLIMA_CPU        CPUs for a new Colima VM (default: 4)
#               COLIMA_MEMORY     GiB of memory for a new Colima VM (default: 8)
#               DOCKER_WAIT_SECS  how long to wait for the engine (default: 120)
#==============================================================================

# Defaults so the library works under `set -u` whatever the caller defined
: "${RED:=\033[0;31m}" "${GREEN:=\033[0;32m}" "${YELLOW:=\033[1;33m}"
: "${CYAN:=\033[0;36m}" "${NC:=\033[0m}"
declare -F debug >/dev/null || debug() { return 0; }

ASSUME_YES="${ASSUME_YES:-false}"
COLIMA_PROFILE="${COLIMA_PROFILE:-default}"
COLIMA_CPU="${COLIMA_CPU:-4}"
COLIMA_MEMORY="${COLIMA_MEMORY:-8}"
DOCKER_WAIT_SECS="${DOCKER_WAIT_SECS:-120}"
MIN_DOCKER_MEM_GIB=6               # chatbot alone is capped at 1 GiB; the full stack needs ~6

PLATFORM_OS=""                     # macOS | Linux | BSD | unknown
PLATFORM_NAME=""                   # human-readable, e.g. "macOS Tahoe 26.1", "Ubuntu 24.04.1 LTS"
PLATFORM_ARCH=""
PKG_MANAGER=""
DOCKER_RUNTIME=""                  # docker context in use once the engine is up

#------------------------------------------------------------------------------
# Output helpers
#------------------------------------------------------------------------------
plat_ok()   { printf '%b\n' "${GREEN}[✓]${NC} $*"; }
plat_info() { printf '%b\n' "${CYAN}[INFO]${NC} $*"; }
plat_warn() { printf '%b\n' "${YELLOW}[!]${NC} $*"; }
plat_err()  { printf '%b\n' "${RED}[ERROR]${NC} $*"; }

# Ask a yes/no question. ASSUME_YES=true skips it; no terminal means no.
confirm() {
    debug "confirm $1"
    local reply
    [[ "$ASSUME_YES" == true ]] && return 0
    if [[ ! -t 0 ]]; then
        plat_warn "No terminal to ask on. Rerun with --yes to allow: $1"
        return 1
    fi
    printf '%b' "${CYAN}[?]${NC} $1 [y/N] "
    read -r reply || reply=""
    case "$reply" in
        [yY]|[yY][eE][sS]) return 0 ;;
        *)                 return 1 ;;
    esac
}

#------------------------------------------------------------------------------
# Platform detection
#------------------------------------------------------------------------------
detect_platform() {
    debug "detect_platform"
    local ver name=""
    PLATFORM_ARCH="$(uname -m)"
    case "$OSTYPE" in
        darwin*)
            PLATFORM_OS="macOS"
            ver="$(sw_vers -productVersion 2>/dev/null || echo unknown)"
            # Apple jumped from 15 to 26 (year-based); unnamed versions print as plain numbers
            case "$ver" in
                26.*) name="Tahoe "    ;;
                15.*) name="Sequoia "  ;;
                14.*) name="Sonoma "   ;;
                13.*) name="Ventura "  ;;
                12.*) name="Monterey " ;;
                11.*) name="Big Sur "  ;;
            esac
            PLATFORM_NAME="macOS ${name}${ver}"
            PKG_MANAGER=""
            command -v brew &>/dev/null && PKG_MANAGER="brew"
            ;;
        linux*)
            PLATFORM_OS="Linux"
            PLATFORM_NAME="$(linux_pretty_name)"
            PKG_MANAGER="$(linux_pkg_manager)"
            ;;
        freebsd*|netbsd*|openbsd*|dragonfly*)
            PLATFORM_OS="BSD"
            PLATFORM_NAME="$OSTYPE"
            ;;
        *)
            PLATFORM_OS="unknown"
            PLATFORM_NAME="$OSTYPE"
            ;;
    esac
    plat_ok "Detected ${PLATFORM_NAME} (${PLATFORM_ARCH})${PKG_MANAGER:+, package manager: ${PKG_MANAGER}}"
}

# Sourced in a subshell: os-release defines VERSION, NAME and ID, and the compose
# files use ${VERSION:-latest} for image tags, so none of it may leak out.
linux_pretty_name() {
    # shellcheck disable=SC1091  # only exists on the Linux host at run time
    ( . /etc/os-release 2>/dev/null && printf '%s' "${PRETTY_NAME:-${NAME:-Linux}}" ) || printf 'Linux'
}

# Decided by which tool exists rather than by distro name, so derivatives
# (Kali, Pop!_OS, Rocky, Manjaro, ...) need no entry of their own.
linux_pkg_manager() {
    local tool
    for tool in apt-get dnf yum pacman zypper apk xbps-install; do
        if command -v "$tool" &>/dev/null; then
            case "$tool" in
                apt-get)      printf 'apt'  ;;
                xbps-install) printf 'xbps' ;;
                *)            printf '%s' "$tool" ;;
            esac
            return 0
        fi
    done
}

#------------------------------------------------------------------------------
# Install hints
#------------------------------------------------------------------------------
print_docker_install_hint() {
    debug "print_docker_install_hint"
    case "$PKG_MANAGER" in
        brew)   printf '  brew install colima docker docker-compose\n' ;;
        apt)    printf '  sudo apt install docker.io\n' ;;
        dnf)    printf '  sudo dnf install moby-engine     # Fedora; RHEL-family uses Docker'"'"'s docker-ce repo\n' ;;
        yum)    printf '  see https://docs.docker.com/engine/install/ for the docker-ce repo\n' ;;
        pacman) printf '  sudo pacman -S docker\n' ;;
        zypper) printf '  sudo zypper install docker\n' ;;
        apk)    printf '  sudo apk add docker\n' ;;
        xbps)   printf '  sudo xbps-install -S docker\n' ;;
        *)      printf '  see https://docs.docker.com/engine/install/\n' ;;
    esac
}

print_compose_install_hint() {
    debug "print_compose_install_hint"
    if [[ "$PLATFORM_OS" == "macOS" ]]; then
        local prefix
        prefix="$(brew --prefix 2>/dev/null || echo /opt/homebrew)"
        printf '  brew install docker-compose\n'
        printf '  # Docker only finds Homebrew'"'"'s plugin if ~/.docker/config.json contains:\n'
        printf '  #   "cliPluginsExtraDirs": ["%s/lib/docker/cli-plugins"]\n' "$prefix"
    else
        printf '  see https://docs.docker.com/compose/install/linux/\n'
    fi
}

#------------------------------------------------------------------------------
# Docker engine checks
#------------------------------------------------------------------------------
docker_ready() { docker info >/dev/null 2>&1; }

wait_for_docker() {
    debug "wait_for_docker"
    local waited=0
    while ! docker_ready; do
        (( waited >= DOCKER_WAIT_SECS )) && return 1
        sleep 2
        waited=$(( waited + 2 ))
    done
    return 0
}

check_docker_memory() {
    debug "check_docker_memory"
    local bytes gib
    bytes="$(docker info --format '{{.MemTotal}}' 2>/dev/null || echo 0)"
    [[ "$bytes" =~ ^[0-9]+$ ]] || return 0
    gib=$(( (bytes + 536870912) / 1073741824 ))   # rounded; an "8 GiB" VM reports a little under 8
    (( gib >= MIN_DOCKER_MEM_GIB )) && return 0
    plat_warn "Docker has only ${gib} GiB of memory; crAPI needs about ${MIN_DOCKER_MEM_GIB} GiB."
    case "$DOCKER_RUNTIME" in
        colima*) printf '  colima stop --profile %s && colima start --profile %s --cpu %s --memory %s\n' \
                     "$COLIMA_PROFILE" "$COLIMA_PROFILE" "$COLIMA_CPU" "$COLIMA_MEMORY" ;;
        desktop*) printf '  Docker Desktop > Settings > Resources > Memory\n' ;;
        *)        printf '  give the Docker VM or host more memory\n' ;;
    esac
    return 0
}

# The docker context Colima creates for a profile
colima_context() {
    if [[ "$COLIMA_PROFILE" == "default" ]]; then printf 'colima'; else printf 'colima-%s' "$COLIMA_PROFILE"; fi
}

#------------------------------------------------------------------------------
# macOS
#------------------------------------------------------------------------------
brew_install() {
    debug "brew_install $*"
    if ! command -v brew &>/dev/null; then
        plat_err "Homebrew is needed to install: $*"
        printf '  Install it from https://brew.sh, then rerun this script.\n'
        return 1
    fi
    if ! confirm "Install $* with Homebrew?"; then
        plat_err "Install them yourself, then rerun:"
        printf '  brew install %s\n' "$*"
        return 1
    fi
    brew install "$@"
}

start_colima() {
    debug "start_colima"
    local dir="${COLIMA_HOME:-$HOME/.colima}/${COLIMA_PROFILE}"
    if [[ -f "${dir}/colima.yaml" ]]; then
        plat_info "Starting Colima (profile ${COLIMA_PROFILE}, settings from ${dir}/colima.yaml)..."
        colima start --profile "$COLIMA_PROFILE" || { plat_err "colima start failed. Try: colima start --verbose"; exit 1; }
    else
        plat_info "Creating Colima VM: ${COLIMA_CPU} CPUs, ${COLIMA_MEMORY} GiB memory..."
        colima start --profile "$COLIMA_PROFILE" --cpu "$COLIMA_CPU" --memory "$COLIMA_MEMORY" \
            || { plat_err "colima start failed. Try: colima start --verbose"; exit 1; }
    fi
    # Colima switches the docker context itself; this covers a context changed since
    if ! docker_ready; then
        plat_info "Switching docker context to $(colima_context)"
        docker context use "$(colima_context)" >/dev/null 2>&1 || true
    fi
    wait_for_docker || { plat_err "Colima is up but Docker isn't answering. Try: colima restart"; exit 1; }
    plat_info "Colima doesn't start at login on its own. To make it: brew services start colima"
}

start_mac_app() {
    debug "start_mac_app $1"
    local app="$1" label="$2"
    plat_info "Starting ${label}..."
    open -a "$app" || { plat_err "Couldn't open ${label}."; exit 1; }
    plat_info "Waiting up to ${DOCKER_WAIT_SECS}s for ${label}'s engine..."
    wait_for_docker || { plat_err "${label} didn't start its engine in time. Open it and check its status."; exit 1; }
}

ensure_runtime_macos() {
    debug "ensure_runtime_macos"
    if command -v docker &>/dev/null && docker_ready; then
        return 0
    fi
    if command -v colima &>/dev/null; then
        # Colima ships no docker CLI of its own
        command -v docker &>/dev/null || brew_install docker docker-compose || exit 1
        start_colima
    elif [[ -d /Applications/Docker.app ]]; then
        start_mac_app Docker "Docker Desktop"
    elif [[ -d /Applications/OrbStack.app ]]; then
        start_mac_app OrbStack OrbStack
    else
        plat_warn "No Docker runtime found. On macOS crAPI runs Docker inside a Colima VM."
        brew_install colima docker docker-compose || exit 1
        start_colima
    fi
}

#------------------------------------------------------------------------------
# Linux
#------------------------------------------------------------------------------
ensure_runtime_linux() {
    debug "ensure_runtime_linux"
    local err
    if ! command -v docker &>/dev/null; then
        plat_err "Docker Engine is not installed. Install it, then rerun:"
        print_docker_install_hint
        exit 1
    fi
    err="$(docker info 2>&1 >/dev/null)" && return 0
    if [[ "$err" == *"permission denied"* ]]; then
        plat_err "Your user can't reach the Docker socket. Add yourself to the docker group, then log out and back in:"
        # shellcheck disable=SC2016  # $USER is for the user to type, not to expand here
        printf '  sudo usermod -aG docker "$USER"\n'
        exit 1
    fi
    plat_warn "The Docker daemon isn't running."
    if command -v systemctl &>/dev/null && [[ -d /run/systemd/system ]]; then
        confirm "Start it with 'sudo systemctl start docker'?" \
            || { plat_err "Start it yourself, then rerun: sudo systemctl start docker"; exit 1; }
        sudo systemctl start docker
    elif command -v service &>/dev/null; then
        confirm "Start it with 'sudo service docker start'?" \
            || { plat_err "Start it yourself, then rerun: sudo service docker start"; exit 1; }
        sudo service docker start
    else
        plat_err "Start the Docker daemon (dockerd), then rerun."
        exit 1
    fi
    wait_for_docker || { plat_err "The Docker daemon didn't come up. Check: sudo journalctl -u docker"; exit 1; }
}

#------------------------------------------------------------------------------
# Entry point
#------------------------------------------------------------------------------
ensure_docker_runtime() {
    debug "ensure_docker_runtime"
    [[ -n "$PLATFORM_OS" ]] || detect_platform
    case "$PLATFORM_OS" in
        macOS) ensure_runtime_macos ;;
        Linux) ensure_runtime_linux ;;
        *)
            plat_err "Docker has no native engine on ${PLATFORM_NAME}. Run crAPI in a Linux VM instead."
            exit 1
            ;;
    esac
    DOCKER_RUNTIME="$(docker context show 2>/dev/null || echo default)"
    plat_ok "Docker engine is running (context: ${DOCKER_RUNTIME})"
    check_docker_memory
}
