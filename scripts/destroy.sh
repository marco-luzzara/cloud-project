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
    ./stop_local.sh
    chown -R 1000 ./volumes
    rm -rf ./volumes
}

main "$@"