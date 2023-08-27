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

    _RESOURCE_ID="$_RESOURCE_ID" \
    _HTTP_METHOD="$_HTTP_METHOD" \
    _REST_API_ID="$_REST_API_ID" \
    _INTEGRATION_URI="arn:aws:apigateway:$_GLOBALS_REGION:lambda:path/2015-03-31/functions/$_ROUTING_LAMBDA_ARN/invocations" \
    _FUNCTION_NAME="$_FUNCTION_NAME" \
    _REQUEST_TEMPLATES="${_REQUEST_TEMPLATES:-}" \
    create_api_lambda_integration
}

main "$@"