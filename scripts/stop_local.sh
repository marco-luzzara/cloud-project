#!/usr/bin/env bash

# template: https://sharats.me/posts/shell-script-best-practices/

set -o errexit
set -o nounset
set -o pipefail
if [[ "${TRACE-0}" == "1" ]]; then
    set -o xtrace
fi

if [[ "${1-}" =~ ^-*h(elp)?$ ]]; then
    echo "Usage: ./$(basename "$0")"
    exit
fi

cd "$(dirname "$0")"

main() {
    TERRAFORM_CONTAINER_NAME="terraform_for_localstack"
    TERRAFORM_VOLUME_DIR="terraform"
    LOCALSTACK_CONTAINER_NAME="localstackmain"
    LOCALSTACK_VOLUME_DIR="localstack_persistence"
    LOCALSTACK_API_KEY="$(cat ../apikey.secret)"
    LOCALSTACK_PORT=4566
    PERSISTENCE=1
    export TERRAFORM_CONTAINER_NAME
    export TERRAFORM_VOLUME_DIR
    export LOCALSTACK_CONTAINER_NAME
    export LOCALSTACK_VOLUME_DIR
    export LOCALSTACK_API_KEY
    export LOCALSTACK_PORT
    export PERSISTENCE
    docker-compose down
}

main "$@"