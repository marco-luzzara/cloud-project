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
    . ./utils/lambda-utils.sh
    . ./utils/common-utils.sh
    . ./globals.env

    local LAMBDA_ARN
    LAMBDA_ARN="$( \
        get_retval_from "$( \
            _LAMBDA_NAME="$_LAMBDA_NAME" \
            _FUNCTION_NAME="$_FUNCTION_NAME" \
            create_lambda \
        )" \
    )"

    _RESOURCE_ID="$_RESOURCE_ID" \
    _HTTP_METHOD="$_HTTP_METHOD" \
    _REST_API_ID="$_REST_API_ID" \
    _INTEGRATION_URI="arn:aws:apigateway:$_GLOBALS_REGION:lambda:path/2015-03-31/functions/$LAMBDA_ARN/invocations" \
    _REQUEST_TEMPLATES="${_REQUEST_TEMPLATES:-}" \
    create_api_lambda_integration

    return_with "$LAMBDA_ARN"
}

main "$@"