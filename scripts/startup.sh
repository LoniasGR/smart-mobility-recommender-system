#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

set -e

info() {
    echo -e "${GREEN}$*${NC}"
}
error() {
    echo -e "${RED}$*${NC}"
}

command_exists() {
    if ! command -v -- "$1" >/dev/null 2>&1; then
        return 1
    fi
    return 0
}

check_docker() {
    if ! command_exists docker; then
        error "Docker Engine not found! Halting ..."
        exit 1
    else
        info "Docker Engine already installed..."
    fi
}

main() {
    check_docker
}

main
