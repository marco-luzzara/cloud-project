#!/usr/bin/env bash

# template: https://sharats.me/posts/shell-script-best-practices/

set -o errexit
set -o nounset
set -o pipefail
if [[ "${TRACE-0}" == "1" ]]; then
    set -o xtrace
fi

cd "$(dirname "$0")"

main() {
    . ./utils/apigateway-utils.sh
    . ./utils/common-utils.sh
    . ./globals.env

    local CREATED_RESOURCE_ID
    CREATED_RESOURCE_ID="$( \
        get_retval_from "$( \
            _REST_API_ID="$_REST_API_ID" \
            _PARENT_RESOURCE_ID="$_PARENT_RESOURCE_ID" \
            _PATH_PART="$_PATH_PART" \
            create_api_resource \
        )"
    )"

    return_with "$CREATED_RESOURCE_ID"
}

main "$@"