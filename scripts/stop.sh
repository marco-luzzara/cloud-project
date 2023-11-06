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
    if docker network ls | grep --quiet "$LOCALSTACK_NETWORK_NAME"
    then
        CONTAINERS_TO_REMOVE="$(docker network inspect "$LOCALSTACK_NETWORK_NAME" --format='{{range .Containers}}{{.Name}}{{"\n"}}{{end}}')"
        for container in $CONTAINERS_TO_REMOVE; do
            if [[ "$container" != "$LOCALSTACK_CONTAINER_NAME" ]] && [[ "$container" != "$TERRAFORM_CONTAINER_NAME" ]]
            then
                # containers created by the localstack instance should be removed before docker-compose down
                # because they all share the same network
                docker container rm -f "$container"
            fi
        done

        docker-compose down
    fi
}

main "$@"