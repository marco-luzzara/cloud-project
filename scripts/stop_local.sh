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

    set +o errexit

    if docker network ls | grep --quiet localstack_network
    then
        CONTAINERS_TO_REMOVE="$(docker network inspect localstack_network --format='{{range .Containers}}{{.Name}}{{"\n"}}{{end}}')"
        for container in $CONTAINERS_TO_REMOVE; do
            if [[ "$container" != "$LOCALSTACK_CONTAINER_NAME" ]] && [[ "$container" != "$TERRAFORM_CONTAINER_NAME" ]]
            then
                docker container rm -f "$container"
            fi
        done

        docker-compose down
    fi


}

main "$@"