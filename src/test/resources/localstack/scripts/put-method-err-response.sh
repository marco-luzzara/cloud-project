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
    . ./globals.env

    awslocal apigateway put-method-response \
            --rest-api-id "$_REST_API_ID" \
            --resource-id "$_RESOURCE_ID" \
            --http-method "$_HTTP_METHOD" \
            --status-code "$_STATUS_CODE"

    awslocal apigateway put-integration-response \
            --rest-api-id "$_REST_API_ID" \
            --resource-id "$_RESOURCE_ID" \
            --http-method "$_HTTP_METHOD" \
            --status-code "$_STATUS_CODE" \
            --selection-pattern "$_REGEX_ERROR_PATTERN"
}

main "$@"