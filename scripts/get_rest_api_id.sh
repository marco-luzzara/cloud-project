#!/usr/bin/env bash

# template: https://sharats.me/posts/shell-script-best-practices/

set -o errexit
set -o nounset
set -o pipefail
if [[ "${TRACE-0}" == "1" ]]; then
    set -o xtrace
fi

if [[ "${1-}" =~ ^-*h(elp)?$ ]]; then
    echo "Usage: sudo ./$(basename "$0")"
    exit
fi

cd "$(dirname "$0")"

main() {
    LOCALSTACK_CONTAINER_NAME="localstackmain"
    docker exec "$LOCALSTACK_CONTAINER_NAME" awslocal apigateway get-rest-apis --output text --query "items[0].id"
}

main "$@"