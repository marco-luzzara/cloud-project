terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.17.0"
    }
  }

  required_version = ">= 1.2.0"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default = "us-east-1"
}

# provider in Localstack is overridden by provider_override.tf file because the
# endpoints must be manually set
provider "aws" {
  region = var.aws_region

  default_timeouts {
    create = "2m"
  }
}

module "authentication" {
  source = "./authentication"

  admin_user_credentials = var.admin_user_credentials
}

module "webapp_db" {
  source = "./webapp_db"

  webapp_db_config = var.webapp_db_config
  webapp_db_credentials = var.webapp_db_credentials
}

module "customer_lambda" {
  source = "./lambda/customer_lambda"

  customer_lambda_dist_bucket = var.customer_lambda_dist_bucket
  customer_lambda_dist_bucket_key = var.customer_lambda_dist_bucket_key
  customer_lambda_dist_path = var.customer_lambda_dist_path
  customer_lambda_iam_role_arn = var.customer_lambda_iam_role_arn
  is_testing = var.is_testing
  customer_lambda_system_properties = {
    cognito_main_user_pool_id = module.authentication.cognito_main_pool_id
    cognito_main_user_pool_client_id = module.authentication.cognito_main_pool_client_id
    cognito_main_user_pool_client_secret = module.authentication.cognito_main_pool_client_secret
    spring_active_profile = var.customer_lambda_spring_active_profile
    spring_datasource_url = "jdbc:postgresql://${module.webapp_db.rds_endpoint}/${var.webapp_db_config.db_name}"
    spring_datasource_username = var.webapp_db_credentials.username
    spring_datasource_password = var.webapp_db_credentials.password
  }
}

module "admin_lambda" {
  source = "./lambda/admin_lambda"

  admin_lambda_dist_bucket = var.admin_lambda_dist_bucket
  admin_lambda_dist_bucket_key = var.admin_lambda_dist_bucket_key
  admin_lambda_dist_path = var.admin_lambda_dist_path
  admin_lambda_iam_role_arn = var.admin_lambda_iam_role_arn
  is_testing = var.is_testing
  admin_lambda_system_properties = {
    cognito_main_user_pool_id = module.authentication.cognito_main_pool_id
    cognito_main_user_pool_client_id = module.authentication.cognito_main_pool_client_id
    cognito_main_user_pool_client_secret = module.authentication.cognito_main_pool_client_secret
    spring_active_profile = var.admin_lambda_spring_active_profile
    spring_datasource_url = "jdbc:postgresql://${module.webapp_db.rds_endpoint}/${var.webapp_db_config.db_name}"
    spring_datasource_username = var.webapp_db_credentials.username
    spring_datasource_password = var.webapp_db_credentials.password
  }
}

module "webapp_apigw" {
  depends_on = [module.customer_lambda, module.admin_lambda]
  source = "./webapp_apigw"

  customer_lambda_invoke_arn = module.customer_lambda.customer_lambda_invoke_arn
  admin_lambda_invoke_arn = module.admin_lambda.admin_lambda_invoke_arn
  cognito_user_pool_arn = module.authentication.cognito_main_pool_arn
  #  when = terraform.workspace == "webapp"
}

resource "aws_api_gateway_deployment" "apigw_deployment" {
  depends_on = [module.webapp_apigw, module.webapp_db]

  rest_api_id = module.webapp_apigw.webapp_apigw_rest_api_id
  stage_name  = var.apigateway_stage_name
}