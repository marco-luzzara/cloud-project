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
    echo "It is a utility script for when the lamdba cannot start because of a startup exception. In this case, the lambda immediately fails and reading logs is hard"
    exit
fi

cd "$(dirname "$0")"

main() {
    while true
    do
        container_id="$(docker ps --filter "ancestor=public.ecr.aws/lambda/java:17" --format "{{.ID}}")"
        if [[ -n "$container_id" ]]
        then
            docker logs --follow "$container_id"
        fi
    done
}

main "$@"