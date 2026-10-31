#!/bin/bash

# crAPI Hacking Lab Setup & Development Script
# Automated setup for API security testing and vulnerability exploitation practice
# Based on OWASP API Top 10 vulnerabilities

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Platform detection and Docker runtime bootstrap (Colima on macOS), shared with quickstart.sh
# shellcheck source=scripts/lib/platform.sh
source "$SCRIPT_DIR/scripts/lib/platform.sh"

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to check Docker is installed and its engine is running (starts Colima on macOS)
check_docker() {
    [[ -n "$PLATFORM_OS" ]] || detect_platform
    ensure_docker_runtime
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose is not installed. Install it, then rerun:"
        print_compose_install_hint
        exit 1
    fi
}

# Function to check if service dependencies are installed
check_service_deps() {
    local service=$1
    case $service in
        "identity")
            if ! command -v java &> /dev/null; then
                print_warning "Java is not installed. Some operations may fail."
                return 1
            fi
            ;;
        "community")
            if ! command -v go &> /dev/null; then
                print_warning "Go is not installed. Some operations may fail."
                return 1
            fi
            ;;
        "workshop"|"chatbot")
            if ! command -v python3 &> /dev/null; then
                print_warning "Python3 is not installed. Some operations may fail."
                return 1
            fi
            ;;
        "web")
            if ! command -v npm &> /dev/null; then
                print_warning "Node.js/npm is not installed. Some operations may fail."
                return 1
            fi
            ;;
    esac
    return 0
}

# Hacking Lab Quick Setup Functions
hacking_lab_quickstart() {
    print_info "${CYAN}=== crAPI Hacking Lab Quick Setup ===${NC}"
    echo -e "\n${YELLOW}This will set up crAPI as a vulnerable API testing environment${NC}"
    echo -e "${YELLOW}Perfect for practicing OWASP API Top 10 vulnerabilities${NC}\n"
    
    # Check prerequisites
    check_docker
    
    # Pull latest images; chatbot, community and web are built from local source instead
    print_info "Pulling latest crAPI images and building local ones..."
    cd deploy/docker
    docker compose pull --ignore-buildable
    docker compose build
    
    # Configure for hacking lab
    echo -e "\n${CYAN}Select vulnerability configuration:${NC}"
    echo "1) Standard vulnerabilities (Default)"
    echo "2) Enable Shell Injection vulnerability"
    echo "3) Enable Log4j vulnerability"
    echo "4) Enable ALL vulnerabilities (Maximum risk)"
    read -p "Choice [1]: " vuln_choice
    
    ENABLE_SHELL="false"
    ENABLE_LOG4J="false"
    
    case $vuln_choice in
        2)
            ENABLE_SHELL="true"
            print_warning "Shell injection enabled - Use with caution!"
            ;;
        3)
            ENABLE_LOG4J="true"
            print_warning "Log4j vulnerability enabled - Use with caution!"
            ;;
        4)
            ENABLE_SHELL="true"
            ENABLE_LOG4J="true"
            print_warning "ALL vulnerabilities enabled - Maximum risk configuration!"
            ;;
    esac
    
    # Start with appropriate configuration
    print_info "Starting crAPI hacking lab..."
    LISTEN_IP="0.0.0.0" \
    ENABLE_SHELL_INJECTION=$ENABLE_SHELL \
    ENABLE_LOG4J=$ENABLE_LOG4J \
    docker compose -f docker-compose.yml --compatibility up -d
    
    if [ $? -eq 0 ]; then
        print_success "${GREEN}crAPI Hacking Lab is ready!${NC}"
        show_hacking_info
    else
        print_error "Failed to start hacking lab"
    fi
    
    cd "$SCRIPT_DIR"
}

show_hacking_info() {
    echo -e "\n${CYAN}========== Hacking Lab Information ==========${NC}"
    echo -e "${GREEN}Access Points:${NC}"
    echo "  Web UI: http://localhost:8889 (or https://localhost:8443)"
    echo "  Mailhog: http://localhost:8025"
    echo ""
    echo -e "${GREEN}API Endpoints:${NC}"
    echo "  Identity Service: http://localhost:8090"
    echo "  Community Service: http://localhost:8087"
    echo "  Workshop Service: http://localhost:8000"
    echo "  Chatbot Service: http://localhost:5002"
    echo ""
    echo -e "${GREEN}Default Credentials:${NC}"
    echo "  Admin: admin@example.com / Admin!123"
    echo "  Database: admin / crapisecretpassword"
    echo ""
    echo -e "${YELLOW}Available Challenges (18+):${NC}"
    echo "  - BOLA (Broken Object Level Authorization)"
    echo "  - Broken User Authentication"
    echo "  - Excessive Data Exposure"
    echo "  - Rate Limiting vulnerabilities"
    echo "  - BFLA (Broken Function Level Authorization)"
    echo "  - Mass Assignment"
    echo "  - SSRF vulnerabilities"
    echo "  - NoSQL/SQL Injection"
    echo "  - JWT vulnerabilities"
    echo "  - LLM vulnerabilities (prompt injection)"
    echo ""
    echo -e "${CYAN}Documentation:${NC}"
    echo "  Challenges: docs/challenges.md"
    echo "  OpenAPI Spec: openapi-spec/"
    echo "  Postman Collections: postman_collections/"
    echo -e "${CYAN}=============================================${NC}"
}

import_test_data() {
    print_info "Importing test data and creating user accounts..."
    cd deploy/docker
    
    # Wait for services to be healthy
    print_info "Waiting for services to be ready..."
    sleep 10
    
    # Check if services are healthy
    if docker compose exec -T crapi-identity /app/health.sh > /dev/null 2>&1; then
        print_success "Services are ready"
        
        # Create test users
        echo -e "\n${CYAN}Creating test user accounts...${NC}"
        # This would typically involve API calls to create test accounts
        # For now, we'll just inform about the default account
        echo "  Default admin account: admin@example.com / Admin!123"
        echo "  You can create additional accounts through the web UI"
    else
        print_warning "Services not fully ready yet. Please wait and try again."
    fi
    
    cd "$SCRIPT_DIR"
}

# Docker Compose Functions
docker_start() {
    print_info "Starting crAPI with Docker Compose..."
    cd deploy/docker
    
    echo -e "\nSelect network binding:"
    echo "1) Localhost only (127.0.0.1) - Default"
    echo "2) All interfaces (0.0.0.0) - For external access"
    read -p "Choice [1]: " network_choice
    
    case $network_choice in
        2)
            LISTEN_IP="0.0.0.0" docker compose -f docker-compose.yml --compatibility up -d
            ;;
        *)
            docker compose -f docker-compose.yml --compatibility up -d
            ;;
    esac
    
    if [ $? -eq 0 ]; then
        print_success "crAPI started successfully!"
        echo -e "\nAccess points:"
        echo "  Web UI: http://localhost:8889"
        echo "  Mailhog: http://localhost:8025"
    else
        print_error "Failed to start crAPI"
    fi
    cd "$SCRIPT_DIR"
}

docker_stop() {
    print_info "Stopping crAPI..."
    cd deploy/docker
    docker compose down
    print_success "crAPI stopped"
    cd "$SCRIPT_DIR"
}

docker_clean() {
    print_warning "This will stop crAPI and remove all volumes (data will be lost)"
    read -p "Are you sure? (y/N): " confirm
    if [[ $confirm == [yY] ]]; then
        cd deploy/docker
        docker compose down -v
        print_success "crAPI stopped and volumes removed"
        cd "$SCRIPT_DIR"
    fi
}

docker_logs() {
    cd deploy/docker
    echo -e "\nSelect service to view logs:"
    echo "1) All services"
    echo "2) Identity service"
    echo "3) Community service"
    echo "4) Workshop service"
    echo "5) Chatbot service"
    echo "6) Web service"
    echo "7) PostgreSQL"
    echo "8) MongoDB"
    echo "9) Mailhog"
    read -p "Choice: " log_choice
    
    case $log_choice in
        1) docker compose logs -f ;;
        2) docker compose logs -f crapi-identity ;;
        3) docker compose logs -f crapi-community ;;
        4) docker compose logs -f crapi-workshop ;;
        5) docker compose logs -f crapi-chatbot ;;
        6) docker compose logs -f crapi-web ;;
        7) docker compose logs -f postgresdb ;;
        8) docker compose logs -f mongodb ;;
        9) docker compose logs -f mailhog ;;
        *) print_error "Invalid choice" ;;
    esac
    cd "$SCRIPT_DIR"
}

docker_status() {
    print_info "Checking service status..."
    cd deploy/docker
    docker compose ps
    cd "$SCRIPT_DIR"
}

docker_build_all() {
    print_info "Building all Docker images from source..."
    cd deploy/docker
    if [ -f "./build-all.sh" ]; then
        bash ./build-all.sh
        print_success "All images built successfully"
    else
        print_error "build-all.sh script not found"
    fi
    cd "$SCRIPT_DIR"
}

# Service-specific functions
build_identity() {
    print_info "Building Identity Service (Java/Spring Boot)..."
    check_service_deps "identity"
    cd services/identity
    if [ -f "./gradlew" ]; then
        ./gradlew build
        print_success "Identity service built"
    else
        print_error "Gradle wrapper not found"
    fi
    cd "$SCRIPT_DIR"
}

test_identity() {
    print_info "Testing Identity Service..."
    cd services/identity
    if [ -f "./gradlew" ]; then
        ./gradlew test
        ./gradlew spotlessCheck || print_warning "Code formatting issues found. Run './gradlew spotlessApply' to fix."
    else
        print_error "Gradle wrapper not found"
    fi
    cd "$SCRIPT_DIR"
}

build_community() {
    print_info "Building Community Service (Go)..."
    check_service_deps "community"
    cd services/community
    go build
    print_success "Community service built"
    cd "$SCRIPT_DIR"
}

test_community() {
    print_info "Testing Community Service..."
    check_service_deps "community"
    cd services/community
    go test ./...
    go mod tidy
    print_success "Community service tests completed"
    cd "$SCRIPT_DIR"
}

build_workshop() {
    print_info "Setting up Workshop Service (Python/Django)..."
    check_service_deps "workshop"
    cd services/workshop
    
    # Check if virtual environment exists
    if [ ! -d "venv" ]; then
        print_info "Creating virtual environment..."
        python3 -m venv venv
    fi
    
    source venv/bin/activate
    pip install -r requirements.txt
    python manage.py migrate
    deactivate
    print_success "Workshop service setup complete"
    cd "$SCRIPT_DIR"
}

test_workshop() {
    print_info "Testing Workshop Service..."
    check_service_deps "workshop"
    cd services/workshop
    
    if [ -d "venv" ]; then
        source venv/bin/activate
        python manage.py test
        black . --check || print_warning "Code formatting issues found. Run 'black .' to fix."
        deactivate
    else
        print_warning "Virtual environment not found. Run build first."
    fi
    cd "$SCRIPT_DIR"
}

setup_chatbot() {
    print_info "Setting up Chatbot Service (Python)..."
    check_service_deps "chatbot"
    cd services/chatbot
    
    if [ ! -d "venv" ]; then
        print_info "Creating virtual environment..."
        python3 -m venv venv
    fi
    
    source venv/bin/activate
    pip install -r requirements.txt
    if [ -f "requirements-dev.txt" ]; then
        pip install -r requirements-dev.txt
    fi
    deactivate
    print_success "Chatbot service setup complete"
    cd "$SCRIPT_DIR"
}

build_web() {
    print_info "Building Web Service (React)..."
    check_service_deps "web"
    cd services/web
    npm install
    npm run build
    print_success "Web service built"
    cd "$SCRIPT_DIR"
}

test_web() {
    print_info "Testing and linting Web Service..."
    check_service_deps "web"
    cd services/web
    npm run lint || print_warning "Linting issues found. Run 'npm run lint:fix' to fix."
    npm test -- --watchAll=false
    cd "$SCRIPT_DIR"
}

# Health check functions
health_check() {
    print_info "Running health checks..."
    cd deploy/docker
    
    # Check if services are running
    if ! docker compose ps | grep -q "Up"; then
        print_error "No services are running. Start crAPI first."
        cd "$SCRIPT_DIR"
        return
    fi
    
    echo -e "\nChecking service health:"
    
    # Web interface
    if curl -s -f http://localhost:8889/health > /dev/null 2>&1; then
        print_success "Web interface: Healthy"
    else
        print_error "Web interface: Not responding"
    fi
    
    # Individual services
    for service in crapi-identity crapi-community crapi-workshop; do
        if docker compose exec -T $service /app/health.sh > /dev/null 2>&1; then
            print_success "$service: Healthy"
        else
            print_warning "$service: Not healthy or not running"
        fi
    done
    
    cd "$SCRIPT_DIR"
}

# Database access functions
db_access() {
    cd deploy/docker
    echo -e "\nSelect database:"
    echo "1) PostgreSQL"
    echo "2) MongoDB"
    read -p "Choice: " db_choice
    
    case $db_choice in
        1)
            print_info "Connecting to PostgreSQL..."
            docker compose exec postgresdb psql -U admin -d crapi
            ;;
        2)
            print_info "Connecting to MongoDB..."
            docker compose exec mongodb mongo -u admin -p crapisecretpassword
            ;;
        *)
            print_error "Invalid choice"
            ;;
    esac
    cd "$SCRIPT_DIR"
}

# API Testing Tools Setup
setup_testing_tools() {
    print_info "${CYAN}Setting up API testing tools...${NC}"
    echo -e "\nThis will check/install common API testing tools:"
    echo "  - curl (HTTP client)"
    echo "  - httpie (Modern HTTP client)"
    echo "  - jq (JSON processor)"
    echo "  - Postman collections (in repo)"
    echo ""
    
    # Check for tools
    tools_missing=false
    
    if ! command -v curl &> /dev/null; then
        print_warning "curl is not installed"
        tools_missing=true
    else
        print_success "curl is installed"
    fi
    
    if ! command -v http &> /dev/null; then
        print_warning "httpie is not installed (install with: apt install httpie)"
        tools_missing=true
    else
        print_success "httpie is installed"
    fi
    
    if ! command -v jq &> /dev/null; then
        print_warning "jq is not installed (install with: apt install jq)"
        tools_missing=true
    else
        print_success "jq is installed"
    fi
    
    # Check for Postman collections
    if [ -d "postman_collections" ]; then
        print_success "Postman collections found in: postman_collections/"
        ls -la postman_collections/*.json 2>/dev/null || echo "  No collection files found yet"
    fi
    
    # Check for OpenAPI spec
    if [ -d "openapi-spec" ]; then
        print_success "OpenAPI specifications found in: openapi-spec/"
        ls -la openapi-spec/*.json 2>/dev/null || echo "  No spec files found yet"
    fi
    
    if [ "$tools_missing" = true ]; then
        echo -e "\n${YELLOW}Some tools are missing. Would you like installation commands?${NC}"
        read -p "Show installation commands? (y/N): " show_install
        if [[ $show_install == [yY] ]]; then
            echo -e "\n${CYAN}Installation commands for Debian/Ubuntu:${NC}"
            echo "  sudo apt update"
            echo "  sudo apt install curl httpie jq"
            echo -e "\n${CYAN}For other distros, use your package manager.${NC}"
        fi
    fi
}

test_api_endpoint() {
    print_info "Testing API endpoints..."
    
    # Check if crAPI is running
    if ! curl -s -f http://localhost:8889/health > /dev/null 2>&1; then
        print_error "crAPI is not running. Please start it first."
        return
    fi
    
    echo -e "\n${CYAN}Select endpoint to test:${NC}"
    echo "1) Health check (Web UI)"
    echo "2) Identity service health"
    echo "3) Login endpoint (POST)"
    echo "4) Get vehicles (requires auth)"
    echo "5) Custom endpoint"
    read -p "Choice: " endpoint_choice
    
    case $endpoint_choice in
        1)
            print_info "Testing Web UI health..."
            curl -s http://localhost:8889/health | jq . 2>/dev/null || curl -s http://localhost:8889/health
            ;;
        2)
            print_info "Testing Identity service..."
            curl -s http://localhost:8080/identity/health | jq . 2>/dev/null || curl -s http://localhost:8080/identity/health
            ;;
        3)
            print_info "Testing login endpoint..."
            echo '{"email":"admin@example.com","password":"Admin!123"}' | \
            curl -s -X POST http://localhost:8080/identity/api/auth/login \
                 -H "Content-Type: application/json" \
                 -d @- | jq . 2>/dev/null
            ;;
        4)
            print_warning "This requires authentication. Use the login endpoint first to get a token."
            ;;
        5)
            read -p "Enter full URL: " custom_url
            curl -s "$custom_url" | jq . 2>/dev/null || curl -s "$custom_url"
            ;;
    esac
}

export_api_docs() {
    print_info "Exporting API documentation..."
    
    if [ -d "openapi-spec" ]; then
        echo -e "\n${GREEN}OpenAPI specifications available:${NC}"
        ls -1 openapi-spec/*.json 2>/dev/null
        echo -e "\nYou can import these into:"
        echo "  - Postman"
        echo "  - Insomnia"
        echo "  - Swagger UI"
        echo "  - Any OpenAPI-compatible tool"
    fi
    
    if [ -d "postman_collections" ]; then
        echo -e "\n${GREEN}Postman collections available:${NC}"
        ls -1 postman_collections/*.json 2>/dev/null
        echo -e "\nImport these directly into Postman for testing."
    fi
}

# Main menu
show_menu() {
    echo -e "\n${MAGENTA}========== crAPI Hacking Lab & Development Menu ==========${NC}"
    echo -e "${CYAN}Quick Setup:${NC}"
    echo "  ${GREEN}1)  🚀 One-Click Hacking Lab Setup${NC}"
    echo "  2)  📦 Import Test Data & Accounts"
    echo "  3)  🔧 Setup API Testing Tools"
    echo "  4)  📋 Show Lab Information"
    echo ""
    echo -e "${GREEN}Docker Operations:${NC}"
    echo "  5)  Start crAPI (Custom Config)"
    echo "  6)  Stop crAPI"
    echo "  7)  Clean Stop (Remove volumes)"
    echo "  8)  View Logs"
    echo "  9)  Check Status"
    echo "  10) Build All Images from Source"
    echo ""
    echo -e "${GREEN}Service Development:${NC}"
    echo "  11) Build Identity Service (Java)"
    echo "  12) Test Identity Service"
    echo "  13) Build Community Service (Go)"
    echo "  14) Test Community Service"
    echo "  15) Setup Workshop Service (Python)"
    echo "  16) Test Workshop Service"
    echo "  17) Setup Chatbot Service (Python)"
    echo "  18) Build Web Service (React)"
    echo "  19) Test & Lint Web Service"
    echo ""
    echo -e "${GREEN}Testing & Utilities:${NC}"
    echo "  20) Test API Endpoint"
    echo "  21) Run Health Checks"
    echo "  22) Access Database"
    echo "  23) Export API Documentation"
    echo "  24) Pull Latest Images"
    echo ""
    echo "  0)  Exit"
    echo -e "${MAGENTA}========================================================${NC}"
}

# Main loop
main() {
    # Show banner
    echo -e "${MAGENTA}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${MAGENTA}║     ${CYAN}crAPI - Completely Ridiculous API${MAGENTA}                 ║${NC}"
    echo -e "${MAGENTA}║     ${YELLOW}OWASP API Security Testing Environment${MAGENTA}            ║${NC}"
    echo -e "${MAGENTA}╚════════════════════════════════════════════════════════╝${NC}"
    
    check_docker
    
    while true; do
        show_menu
        read -p "Enter choice: " choice
        
        case $choice in
            # Quick Setup
            1) hacking_lab_quickstart ;;
            2) import_test_data ;;
            3) setup_testing_tools ;;
            4) show_hacking_info ;;
            
            # Docker Operations
            5) docker_start ;;
            6) docker_stop ;;
            7) docker_clean ;;
            8) docker_logs ;;
            9) docker_status ;;
            10) docker_build_all ;;
            
            # Service Development
            11) build_identity ;;
            12) test_identity ;;
            13) build_community ;;
            14) test_community ;;
            15) build_workshop ;;
            16) test_workshop ;;
            17) setup_chatbot ;;
            18) build_web ;;
            19) test_web ;;
            
            # Testing & Utilities
            20) test_api_endpoint ;;
            21) health_check ;;
            22) db_access ;;
            23) export_api_docs ;;
            24) 
                cd deploy/docker
                docker compose pull --ignore-buildable
                docker compose build
                print_success "Images pulled and local ones built successfully"
                cd "$SCRIPT_DIR"
                ;;
            
            0) 
                print_info "Exiting crAPI Lab Setup..."
                echo -e "${CYAN}Happy Hacking! 🎯${NC}"
                exit 0
                ;;
            *)
                print_error "Invalid choice. Please try again."
                ;;
        esac
        
        echo -e "\nPress Enter to continue..."
        read
    done
}

# Run main function
main