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
    . ./utils/lambda-utils.sh
    . ./utils/common-utils.sh
    . ./globals.env

    local LAMBDA_ARN
    LAMBDA_ARN="$( \
        get_retval_from "$( \
            _LAMBDA_NAME="$_LAMBDA_NAME" \
            create_lambda \
        )" \
    )"

    return_with "$LAMBDA_ARN"
}

main "$@"