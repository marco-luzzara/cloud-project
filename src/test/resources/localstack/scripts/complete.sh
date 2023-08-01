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
    . ./globals.env

    awslocal apigateway create-deployment --rest-api-id "$_REST_API_ID" --stage-name "$_GLOBALS_DEPLOYMENT_NAME"
}

main "$@"