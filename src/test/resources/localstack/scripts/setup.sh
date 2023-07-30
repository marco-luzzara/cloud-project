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
    printf "%s\n%s\n%s\n%s\n" "$_ACCESS_KEY_ID" "$_SECRET_KEY_ID" "$_REGION" "text" | aws configure
    . ./utils/s3-utils.sh

    local DIST_BUCKET="dist-bucket"
    _BUCKET_NAME="$DIST_BUCKET" create_s3_bucket
    awslocal s3api put-object --bucket "$DIST_BUCKET" --key "dist.zip" --body "/app/dist.zip"
}

main "$@"