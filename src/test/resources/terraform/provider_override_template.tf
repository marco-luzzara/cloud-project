provider "aws" {
  access_key                  = "%1$s"
  secret_key                  = "%2$s"
  region                      = var.aws_region
  s3_use_path_style           = true
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    apigateway     = "%3$s"
    apigatewayv2   = "%3$s"
    cloudformation = "%3$s"
    cloudwatch     = "%3$s"
    dynamodb       = "%3$s"
    ec2            = "%3$s"
    es             = "%3$s"
    elasticache    = "%3$s"
    firehose       = "%3$s"
    iam            = "%3$s"
    kinesis        = "%3$s"
    lambda         = "%3$s"
    rds            = "%3$s"
    redshift       = "%3$s"
    route53        = "%3$s"
    s3             = "%3$s"
    secretsmanager = "%3$s"
    ses            = "%3$s"
    sns            = "%3$s"
    sqs            = "%3$s"
    ssm            = "%3$s"
    stepfunctions  = "%3$s"
    sts            = "%3$s"
  }
}