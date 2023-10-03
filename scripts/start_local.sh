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

print_step_message() {
    echo "*************** $1 ..."
}

print_done() {
    echo "*************** Done"
}

main() {
    # create hot-reload folder for lambda hot-reloading
    ( cd .. && gradle buildHotReloadFolder )

    # TODO: add possibility to configure these variables using cmd arguments
    TERRAFORM_CONTAINER_NAME="terraform_for_localstack"
    TERRAFORM_VOLUME_DIR="terraform"
    LOCALSTACK_CONTAINER_NAME="localstackmain"
    LOCALSTACK_VOLUME_DIR="localstack_persistence"
    LOCALSTACK_API_KEY="$(cat ../cloud/infrastructure/src/testFixtures/resources/localstack/apikey.secret)"
    LOCALSTACK_PORT=4566
    PERSISTENCE=1
    export TERRAFORM_CONTAINER_NAME
    export TERRAFORM_VOLUME_DIR
    export LOCALSTACK_CONTAINER_NAME
    export LOCALSTACK_VOLUME_DIR
    export LOCALSTACK_API_KEY
    export LOCALSTACK_PORT
    export PERSISTENCE
    docker-compose up -d

    trap './stop_local.sh' ERR

    ./apply_tf_changes.sh
}

main "$@"