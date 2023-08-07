#!/usr/bin/env bash

# https://docs.aws.amazon.com/apigateway/latest/developerguide/create-api-using-awscli.html
create_rest_api() {
    local CREATED_REST_API_ID
    CREATED_REST_API_ID="$(
        awslocal apigateway create-rest-api \
            --name "$_GLOBALS_REST_API_NAME" \
            --query "id"
    )"

    return_with "$CREATED_REST_API_ID"
}

get_root_resource_id() {
    local API_ROOT_RESOURCE_ID
    API_ROOT_RESOURCE_ID="$(
        awslocal apigateway get-resources \
            --rest-api-id "$_REST_API_ID" \
            --query "items[0].id"
    )"

    return_with "$API_ROOT_RESOURCE_ID"
}

create_api_resource() {
    local CREATED_RESOURCE_ID
    CREATED_RESOURCE_ID="$(
        awslocal apigateway create-resource \
            --rest-api-id "$_REST_API_ID" \
            --parent-id "$_PARENT_RESOURCE_ID" \
            --path-part "$_PATH_PART" \
            --query "id"
    )"

    return_with "$CREATED_RESOURCE_ID"
}

create_api_lambda_integration() {
    awslocal apigateway put-method \
        --rest-api-id "$_REST_API_ID" \
        --resource-id "$_RESOURCE_ID" \
        --http-method "$_HTTP_METHOD" \
        --authorization-type "NONE"

    awslocal apigateway put-method-response \
        --rest-api-id "$_REST_API_ID" \
        --resource-id "$_RESOURCE_ID" \
        --http-method "$_HTTP_METHOD" \
        --status-code 200

    [[ -n "$_REQUEST_TEMPLATES" ]] && conditional_params+=(--request-templates "$_REQUEST_TEMPLATES")
    awslocal apigateway put-integration \
        --rest-api-id "$_REST_API_ID" \
        --resource-id "$_RESOURCE_ID" \
        --http-method "$_HTTP_METHOD" \
        --type AWS \
        --integration-http-method POST \
        --uri "$_INTEGRATION_URI" \
        --passthrough-behavior WHEN_NO_MATCH \
        "${conditional_params[@]}"

    awslocal apigateway put-integration-response \
        --rest-api-id "$_REST_API_ID" \
        --resource-id "$_RESOURCE_ID" \
        --http-method "$_HTTP_METHOD" \
        --status-code 200
}