variable "aws_access_key" {
  description = "AWS Account Access Key"
  type        = string
  sensitive   = true
}

variable "aws_secret_key" {
  description = "AWS Account Secret Key"
  type        = string
  sensitive   = true
}

variable "localstack_hostname" {
  description = "The hostname of the localstack container"
  type        = string
}

variable "localstack_port" {
  description = "The port of the localstack container"
  type        = number
  default = 4566
}

locals {
  service_endpoint = "http://${var.localstack_hostname}:${var.localstack_port}"
}

provider "aws" {
  access_key                  = var.aws_access_key
  secret_key                  = var.aws_secret_key
  region                      = var.aws_region
  s3_use_path_style           = false
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    apigateway     = local.service_endpoint
    apigatewayv2   = local.service_endpoint
    cloudformation = local.service_endpoint
    cloudwatch     = local.service_endpoint
    dynamodb       = local.service_endpoint
    ec2            = local.service_endpoint
    es             = local.service_endpoint
    elasticache    = local.service_endpoint
    firehose       = local.service_endpoint
    iam            = local.service_endpoint
    kinesis        = local.service_endpoint
    lambda         = local.service_endpoint
    rds            = local.service_endpoint
    redshift       = local.service_endpoint
    route53        = local.service_endpoint
    s3             = local.service_endpoint
    secretsmanager = local.service_endpoint
    ses            = local.service_endpoint
    sns            = local.service_endpoint
    sqs            = local.service_endpoint
    ssm            = local.service_endpoint
    stepfunctions  = local.service_endpoint
    sts            = local.service_endpoint
  }
}