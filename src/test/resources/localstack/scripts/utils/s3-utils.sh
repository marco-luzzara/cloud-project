#!/usr/bin/env bash

create_s3_bucket() {
    awslocal s3api create-bucket --bucket "$_BUCKET_NAME"
}