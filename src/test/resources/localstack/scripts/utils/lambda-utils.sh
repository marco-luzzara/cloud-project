#!/usr/bin/env bash

create_lambda() {
    # To call the lambda:
    # awslocal lambda invoke --function-name tenant_setup out --log-type Tail --query 'LogResult' --output text --debug |  base64 -d
    # https://docs.aws.amazon.com/lambda/latest/dg/gettingstarted-awscli.html
    echo "Creating Lambda function..."
    local LAMBDA_ARN
    LAMBDA_ARN="$(
        awslocal lambda create-function \
            --function-name "$_LAMBDA_NAME" \
            --runtime "java17" \
            --handler "org.springframework.cloud.function.adapter.aws.FunctionInvoker" \
            --code "S3Bucket=$_GLOBALS_DIST_S3_BUCKET,S3Key=$_GLOBALS_DIST_S3_KEY" \
            --role "$_GLOBALS_ROLE_ARN" \
            --timeout 900 \
            --environment "{
                            \"Variables\": {\"JAVA_TOOL_OPTIONS\": \"-DMAIN_CLASS=it.unimi.cloudproject.CloudProjectApplication \
                                                                     -Dlogging.level.org.springframework=INFO \
                                                                     -Dspring.profiles.active=localstack\"}
                          }" \
            --query "FunctionArn"
    )"

    # lambda is initially in the pending state, I can use it only when it switches to Active
    # https://docs.aws.amazon.com/lambda/latest/dg/f    unctions-states.html
    awslocal lambda wait function-active-v2 --function-name "$_LAMBDA_NAME"

    return_with "$LAMBDA_ARN"
}