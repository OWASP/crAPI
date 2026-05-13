#!/usr/bin/env bash
#==============================================================================
# SCRIPT:       quickstart.sh
# DESCRIPTION:  One-line automated deployment tool for the crAPI intentionally
#               vulnerable API lab. Designed for rapid setup of OWASP API Top 10
#               security testing environments. Deploys all microservices, databases,
#               and dependencies via Docker Compose with configurable vulnerability
#               profiles and network binding options.
# USAGE:        ./quickstart.sh [OPTIONS]
# OPTIONS:
#               --vulnerable      Enable all optional vulnerabilities including
#                                 shell injection and Log4j exploits for advanced
#                                 security testing scenarios
#               --localhost-only  Bind services to 127.0.0.1 only instead of all
#                                 network interfaces (default: 0.0.0.0)
#               --help            Display usage information and exit
# DEPENDENCIES: Docker >= 20.10
#               Docker Compose plugin >= 1.27.0
# AUTHOR:       LinuxUser255
# REPO:         https://github.com/LinuxUser255/crAPI
#==============================================================================

set -euo pipefail  # §4 strict error handling (-e errexit, -u nounset, pipefail)

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# Script-wide state (all globals initialized to satisfy §4 set -u)
ENABLE_VULNERABILITIES=false
LOCALHOST_ONLY=false
DEBUG="${DEBUG:-0}"                # §4 default value to satisfy set -u
MIN_COMPOSE_VER="1.27.0"
COMPOSE_CMD=""
COMPOSE_VER=""
LISTEN_IP=""
ENABLE_SHELL_INJECTION="false"
ENABLE_LOG4J="false"
HEALTHY=true
SCRIPT_DIR=""

# §5 debug helper — emits only when DEBUG=1
debug() { [[ "$DEBUG" == 1 ]] && echo "[DEBUG] $*"; return 0; }  # §5 debug function; return 0 prevents set -e triggering when DEBUG=0

#------------------------------------------------------------------------------
# Output helpers
#------------------------------------------------------------------------------
print_banner() {
    debug "print_banner"  # §5 debug at function start
    printf '%b\n' "${CYAN}╔════════════════════════════════════════════════════════╗${NC}"  # §3 printf for formatted output
    printf '%b\n' "${CYAN}║        crAPI Hacking Lab - Automated Setup            ║${NC}"
    printf '%b\n' "${CYAN}║           OWASP API Top 10 Testing Environment        ║${NC}"
    printf '%b\n' "${CYAN}╚════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

show_help() {
    debug "show_help"  # §5 debug at function start
    printf '%b\n' "${CYAN}crAPI Hacking Lab Quick Setup${NC}"                                                 # §3 printf + color
    printf '%b\n' "One-line automated deployment for the crAPI OWASP API Top 10 testing environment."        # §3 printf
    printf '\n'
    printf '%b\n' "${CYAN}Usage:${NC} $0 [options]"                                                          # §3 printf + color
    printf '\n'
    printf '%b\n' "${CYAN}Options:${NC}"                                                                     # §3 printf + color
    printf '%b\n' "  ${YELLOW}--vulnerable${NC}      Enable all vulnerabilities (shell injection, log4j)"     # §3 printf + color
    printf '%b\n' "  ${YELLOW}--localhost-only${NC}  Bind only to localhost (default: all interfaces)"        # §3 printf + color
    printf '%b\n' "  ${YELLOW}--help${NC}            Show this help message"                                  # §3 printf + color
    printf '\n'
    printf '%b\n' "${CYAN}Examples:${NC}"                                                                    # §3 printf + color
    printf '%b\n' "  $0                    ${GREEN}# Standard setup with external access${NC}"                # §3 printf + color
    printf '%b\n' "  $0 --vulnerable       ${GREEN}# Enable all vulnerabilities${NC}"                         # §3 printf + color
    printf '%b\n' "  $0 --localhost-only   ${GREEN}# Restrict to localhost only${NC}"                         # §3 printf + color
}

print_docker_build_instructions() {
    debug "print_docker_build_instructions"  # §5 debug at function start
    printf '%b\n' "${CYAN}[ACTION REQUIRED]${NC} Build Docker Engine from source:"
    echo ""
    printf '%b\n' "${CYAN}# Install Go build dependencies (standard repos only, safe)${NC}"
    printf '%b\n' "${CYAN}sudo apt install -y golang-go git make${NC}"
    echo ""
    printf '%b\n' "${CYAN}# Clone and build Docker Engine${NC}"
    printf '%b\n' "${CYAN}git clone https://github.com/moby/moby.git${NC}"
    printf '%b\n' "${CYAN}cd moby${NC}"
    printf '%b\n' "${CYAN}make binary${NC}"
    printf '%b\n' "${CYAN}sudo cp bundles/binary-daemon/dockerd /usr/local/bin/dockerd${NC}"
    printf '%b\n' "${CYAN}sudo cp bundles/binary-daemon/docker /usr/local/bin/docker${NC}"
}

print_compose_build_instructions() {
    debug "print_compose_build_instructions"  # §5 debug at function start
    printf '%b\n' "${CYAN}# Build Docker Compose V2 from source${NC}"
    printf '%b\n' "${CYAN}git clone https://github.com/docker/compose.git${NC}"
    printf '%b\n' "${CYAN}cd compose${NC}"
    printf '%b\n' "${CYAN}make${NC}"
    printf '%b\n' "${CYAN}mkdir -p ~/.docker/cli-plugins${NC}"
    printf '%b\n' "${CYAN}cp bin/build/docker-compose ~/.docker/cli-plugins/docker-compose${NC}"
    printf '%b\n' "${CYAN}chmod +x ~/.docker/cli-plugins/docker-compose${NC}"
    printf '%b\n' "${CYAN}# Verify${NC}"
    printf '%b\n' "${CYAN}docker compose version${NC}"
}

#------------------------------------------------------------------------------
# Argument parsing — §7 user input validated via case block
#------------------------------------------------------------------------------
parse_args() {
    debug "parse_args $*"  # §5 debug at function start
    # §4 positional-independent --help check; ${1:-} / ${*} safe under set -u
    [[ "${1:-}" == "--help" ]] || [[ "${*}" == *"--help"* ]] && { show_help; exit 0; }
    local arg  # §8 local variable
    for arg in "$@"; do
        case "$arg" in  # §4 quoted variable in case
            --vulnerable)
                ENABLE_VULNERABILITIES=true
                ;;
            --localhost-only)
                LOCALHOST_ONLY=true
                ;;
            --help)
                show_help
                exit 0
                ;;
        esac
    done
}

#------------------------------------------------------------------------------
# Sever comparison — pure bash, no external forks
#------------------------------------------------------------------------------
version_ge() {  # §8 brace-form function to allow `local`
    debug "version_ge $1 >= $2"  # §5 debug at function start
    local a="$1" b="$2"                 # §8 local
    local ma1 mi1 pa1 ma2 mi2 pa2       # §8 local
    local IFS='.'                        # §8 local (scoped IFS)
    # shellcheck disable=SC2086  # intentional unquoted split on '.'
    set -- $a; ma1=$1; mi1=$2; pa1=${3:-0}
    # shellcheck disable=SC2086  # intentional unquoted split on '.'
    set -- $b; ma2=$1; mi2=$2; pa2=${3:-0}
    (( ma1 > ma2 )) && return 0          # §3 arithmetic context
    (( ma1 < ma2 )) && return 1          # §3 arithmetic context
    (( mi1 > mi2 )) && return 0          # §3 arithmetic context
    (( mi1 < mi2 )) && return 1          # §3 arithmetic context
    (( pa1 >= pa2 ))                     # §3 arithmetic context
}

#------------------------------------------------------------------------------
# Docker Engine pre-flight check
#------------------------------------------------------------------------------
check_docker() {
    debug "check_docker"  # §5 debug at function start
    if ! command -v docker &>/dev/null; then
        printf '%b\n' "${RED}[ERROR]${NC} Docker Engine is not installed!"
        echo ""
        print_docker_build_instructions
        echo ""
        printf '%b\n' "${CYAN}[ACTION REQUIRED]${NC} After building Docker, build Docker Compose V2:"
        echo ""
        print_compose_build_instructions
        echo ""
        exit 1
    fi
}

#------------------------------------------------------------------------------
# Docker Compose V2 detection and validation
#------------------------------------------------------------------------------
detect_compose() {
    debug "detect_compose"  # §5 debug at function start
    local raw  # §8 local variable

    # V2 plugin is mandatory
    if docker compose version &>/dev/null; then
        COMPOSE_CMD="docker compose"
        raw="$(docker compose version --short 2>/dev/null || docker compose version)"  # §4 quoted
        if [[ "$raw" =~ ([0-9]+\.[0-9]+\.[0-9]+) ]]; then  # §1/§3 bash regex replaces grep fork
            COMPOSE_VER="${BASH_REMATCH[1]}"
        else
            COMPOSE_VER="$raw"
        fi
        return 0
    fi

    # V2 not available — abort regardless of V1 presence
    printf '%b\n' "${RED}[ERROR]${NC} Docker Compose V2 plugin is not available or not functioning."
    echo ""
    printf '%b\n' "${CYAN}[ACTION REQUIRED]${NC} Build Docker Compose V2 from source:"
    echo ""
    print_compose_build_instructions
    echo ""

    # §4 V1 detection via command -v only; binary is never executed
    if command -v docker-compose &>/dev/null; then
        printf '%b\n' "${YELLOW}[INFO]${NC} docker-compose V1 was detected but is deprecated and unsupported."
    fi

    exit 1
}

validate_compose_version() {
    debug "validate_compose_version"  # §5 debug at function start
    if ! version_ge "$COMPOSE_VER" "$MIN_COMPOSE_VER"; then
        printf '%b\n' "${RED}[ERROR]${NC} Docker Compose ${COMPOSE_VER} is too old (minimum: ${MIN_COMPOSE_VER})"
        echo ""
        printf '%b\n' "${CYAN}[ACTION REQUIRED]${NC} Rebuild Docker Compose V2 from source:"
        echo ""
        print_compose_build_instructions
        echo ""
        exit 1
    fi
    printf '%b\n' "${GREEN}[✓]${NC} Docker and Docker Compose ${COMPOSE_VER} detected"
}

#------------------------------------------------------------------------------
# Deployment configuration
#------------------------------------------------------------------------------
configure_environment() {
    debug "configure_environment"  # §5 debug at function start
    if [[ "$LOCALHOST_ONLY" == true ]]; then  # §3 [[ ]] instead of [ ]
        LISTEN_IP="127.0.0.1"
        printf '%b\n' "${YELLOW}[CONFIG]${NC} Binding to localhost only"
    else
        LISTEN_IP="0.0.0.0"
        printf '%b\n' "${YELLOW}[CONFIG]${NC} Binding to all network interfaces"
    fi

    if [[ "$ENABLE_VULNERABILITIES" == true ]]; then  # §3 [[ ]] instead of [ ]
        ENABLE_SHELL_INJECTION="true"
        ENABLE_LOG4J="true"
        printf '%b\n' "${YELLOW}[WARNING]${NC} All vulnerabilities enabled - Maximum risk configuration!"
    else
        ENABLE_SHELL_INJECTION="false"
        ENABLE_LOG4J="false"
        printf '%b\n' "${GREEN}[CONFIG]${NC} Standard vulnerability configuration"
    fi
}

#------------------------------------------------------------------------------
# Stack lifecycle — pull, clean, start
#------------------------------------------------------------------------------
deploy_stack() {
    debug "deploy_stack"  # §5 debug at function start
    printf '%b\n' "${CYAN}[1/4]${NC} Pulling latest crAPI images..."
    # shellcheck disable=SC2086  # §4 intentional word split: "docker compose" must expand to 2 tokens
    $COMPOSE_CMD pull

    printf '%b\n' "${CYAN}[2/4]${NC} Cleaning up any existing instances..."
    # shellcheck disable=SC2086  # §4 intentional word split on $COMPOSE_CMD
    $COMPOSE_CMD down -v 2>/dev/null || true

    printf '%b\n' "${CYAN}[3/4]${NC} Starting crAPI hacking lab..."
    LISTEN_IP="$LISTEN_IP" \
    ENABLE_SHELL_INJECTION="$ENABLE_SHELL_INJECTION" \
    ENABLE_LOG4J="$ENABLE_LOG4J" \
    TLS_ENABLED="true" \
    $COMPOSE_CMD -f docker-compose.yml --compatibility up -d  # §4 intentional split on $COMPOSE_CMD

    printf '%b\n' "${CYAN}[4/4]${NC} Waiting for services to be ready..."
    sleep 15
}

#------------------------------------------------------------------------------
# Health checks
#------------------------------------------------------------------------------
check_health() {
    debug "check_health"  # §5 debug at function start
    local service  # §8 local variable
    HEALTHY=true
    if curl -s -f http://localhost:8888/health > /dev/null 2>&1; then
        printf '%b\n' "${GREEN}[✓]${NC} Web interface is ready"
    else
        printf '%b\n' "${YELLOW}[!]${NC} Web interface not responding yet"
        HEALTHY=false
    fi

    for service in crapi-identity crapi-community crapi-workshop; do
        # shellcheck disable=SC2086  # §4 intentional word split on $COMPOSE_CMD
        if $COMPOSE_CMD exec -T "$service" /app/health.sh > /dev/null 2>&1; then  # §6 quoted path variable
            printf '%b\n' "${GREEN}[✓]${NC} $service is healthy"
        else
            printf '%b\n' "${YELLOW}[!]${NC} $service is still starting..."
            HEALTHY=false
        fi
    done
}

#------------------------------------------------------------------------------
# Final summary output
#------------------------------------------------------------------------------
print_summary() {
    debug "print_summary"  # §5 debug at function start
    echo ""
    printf '%b\n' "${GREEN}════════════════════════════════════════════════════════${NC}"
    printf '%b\n' "${GREEN}       crAPI Hacking Lab Successfully Deployed!         ${NC}"
    printf '%b\n' "${GREEN}════════════════════════════════════════════════════════${NC}"
    echo ""
    printf '%b\n' "${CYAN}Access Points:${NC}"
    printf '%b\n' "  Web UI:        ${GREEN}http://localhost:8888${NC}"
    printf '%b\n' "  Web UI (TLS):  ${GREEN}https://localhost:8443${NC}"
    printf '%b\n' "  Mailhog:       ${GREEN}http://localhost:8025${NC}"
    echo ""
    printf '%b\n' "${CYAN}API Endpoints:${NC}"
    printf '%b\n' "  Identity:      http://localhost:8090"  # <-- changed from 8080 to avoid conflict with BurpSuite
    printf '%b\n' "  Community:     http://localhost:8087"
    printf '%b\n' "  Workshop:      http://localhost:8000"
    printf '%b\n' "  Chatbot:       http://localhost:5002"
    echo ""
    printf '%b\n' "${CYAN}Default Credentials:${NC}"
    printf '%b\n' "  Email:         ${YELLOW}admin@example.com${NC}"
    printf '%b\n' "  Password:      ${YELLOW}Admin!123${NC}"
    echo ""
    printf '%b\n' "${CYAN}Quick Commands:${NC}"
    printf '%b\n' "  View logs:     ${YELLOW}cd deploy/docker && docker compose logs -f${NC}"
    printf '%b\n' "  Stop lab:      ${YELLOW}cd deploy/docker && docker compose down${NC}"
    printf '%b\n' "  Clean stop:    ${YELLOW}cd deploy/docker && docker compose down -v${NC}"
    echo ""
    printf '%b\n' "${CYAN}Documentation:${NC}"
    printf '%b\n' "  Challenges:    ${YELLOW}${SCRIPT_DIR}/docs/challenges.md${NC}"  # §6 quoted path variable
    printf '%b\n' "  API Specs:     ${YELLOW}${SCRIPT_DIR}/openapi-spec/${NC}"       # §6 quoted path variable
    printf '%b\n' "  Postman:       ${YELLOW}${SCRIPT_DIR}/postman_collections/${NC}" # §6 quoted path variable
    echo ""

    if [[ "$HEALTHY" == false ]]; then  # §3 [[ ]] instead of [ ]
        printf '%b\n' "${YELLOW}Note: Some services are still starting. Wait a moment and try again.${NC}"
        printf '%b\n' "${YELLOW}You can check status with: docker compose ps${NC}"
    fi

    printf '%b\n' "${GREEN}Happy Hacking! 🎯${NC}"
    echo ""
    printf '%b\n' "${CYAN}For more options, run: ./crapi-dev.sh${NC}"
}

#------------------------------------------------------------------------------
# §9 main() orchestrator
#------------------------------------------------------------------------------
main() {
    debug "main $*"  # §5 debug at function start
    parse_args "$@"
    print_banner
    check_docker
    detect_compose
    validate_compose_version

    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"  # §6 quoted path variable
    cd "${SCRIPT_DIR}/deploy/docker"                           # §6 quoted path variable

    configure_environment
    deploy_stack
    check_health
    print_summary
}

main "$@"  # §9 entry point
