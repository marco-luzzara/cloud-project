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
    . ./utils/s3-utils.sh
    . ./utils/apigateway-utils.sh
    . ./utils/common-utils.sh
    . ./globals.env

    printf "%s\n%s\n%s\n%s\n" "$_ACCESS_KEY_ID" "$_SECRET_KEY_ID" "$_GLOBALS_REGION" "text" | aws configure

    _BUCKET_NAME="$_GLOBALS_DIST_S3_BUCKET" create_s3_bucket
    awslocal s3api put-object --bucket "$_GLOBALS_DIST_S3_BUCKET" --key "$_GLOBALS_DIST_S3_KEY" --body "/app/dist.zip"

    local REST_API_ID
    REST_API_ID="$( get_retval_from "$( create_rest_api )" )"
    local API_ROOT_RESOURCE_ID
    API_ROOT_RESOURCE_ID="$( get_retval_from "$( _REST_API_ID="$REST_API_ID" get_root_resource_id )" )"
    local API_USER_RESOURCE_ID
    API_USER_RESOURCE_ID="$( \
        get_retval_from "$( \
            _REST_API_ID="$REST_API_ID" \
            _PARENT_RESOURCE_ID="$API_ROOT_RESOURCE_ID" \
            _PATH_PART="users" \
            create_api_resource \
        )" \
    )"

    local API_SHOP_RESOURCE_ID
    API_SHOP_RESOURCE_ID="$( \
        get_retval_from "$( \
            _REST_API_ID="$REST_API_ID" \
            _PARENT_RESOURCE_ID="$API_ROOT_RESOURCE_ID" \
            _PATH_PART="shops" \
            create_api_resource \
        )" \
    )"

    SETUP_RESPONSE="{ \
        \"restApiId\": \"$REST_API_ID\", \
        \"apiUsersResourceId\": \"$API_USER_RESOURCE_ID\", \
        \"apiShopsResourceId\": \"$API_SHOP_RESOURCE_ID\" \
    }"
    return_with "$SETUP_RESPONSE"
}

main "$@"