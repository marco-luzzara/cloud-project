terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.17.0"
    }
  }

  required_version = ">= 1.2.0"
}

# provider in Localstack is overridden by provider_override.tf file because the
# endpoints must be manually set
provider "aws" {
  region = var.aws_region
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

resource "aws_s3_bucket" "lambda_bucket" {
  count = var.lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = var.lambda_dist_bucket
}

module "initializer_lambda" {
  source = "./api_lambda"

  lambda_dist_bucket = var.lambda_dist_bucket == "hot-reload" ? var.lambda_dist_bucket : aws_s3_bucket.lambda_bucket[0].bucket
  lambda_dist_bucket_key = var.initializer_lambda_dist_bucket_key
  lambda_dist_path = var.initializer_lambda_dist_path
  webapp_db_arn = module.webapp_db.arn
  lambda_system_properties = {
    logging_level = "INFO"
    spring_active_profile = var.initializer_lambda_spring_active_profile
    spring_datasource_url = "jdbc:postgresql://${module.webapp_db.rds_endpoint}/${var.webapp_db_config.db_name}"
    spring_datasource_username = var.webapp_db_credentials.username
    spring_datasource_password = var.webapp_db_credentials.password
  }
  function_name = "initializer-lambda"
  is_observability_enabled = false
  is_testing = var.is_testing
  main_class = "it.unimi.cloudproject.Initializer"
  extended_policy_statements = []
}

resource "aws_lambda_invocation" "initializer_execution" {
  function_name = module.initializer_lambda.function_name
  input = jsonencode({})
}

module "customer_lambda" {
  source = "./api_lambda"

  lambda_dist_bucket = var.lambda_dist_bucket == "hot-reload" ? var.lambda_dist_bucket : aws_s3_bucket.lambda_bucket[0].bucket
  lambda_dist_bucket_key = var.customer_lambda_dist_bucket_key
  lambda_dist_path = var.customer_lambda_dist_path
  webapp_db_arn = module.webapp_db.arn
  is_testing = var.is_testing
  is_observability_enabled = var.is_observability_enabled
  lambda_system_properties = {
    logging_level = "INFO"
    spring_active_profile = var.customer_lambda_spring_active_profile
    spring_datasource_url = "jdbc:postgresql://${module.webapp_db.rds_endpoint}/${var.webapp_db_config.db_name}"
    spring_datasource_username = var.webapp_db_credentials.username
    spring_datasource_password = var.webapp_db_credentials.password
  }
  lambda_additional_system_properties = <<EOT
    -Daws.cognito.user_pool_id=${module.authentication.cognito_main_pool_id}
    -Daws.cognito.user_pool_client_id=${module.authentication.cognito_main_pool_client_id}
  EOT
  function_name = "customer-lambda"
  main_class = "it.unimi.cloudproject.CustomerApi"
  extended_policy_statements = [
    {
      Action = [
        "cognito-idp:AdminConfirmSignUp",
        "cognito-idp:SignUp",
        "cognito-idp:AdminDeleteUser",
        "cognito-idp:AdminAddUserToGroup"
      ]
      Effect   = "Allow"
      Resource = ["*"]
    },
    {
      Action = [
        "sns:CreateTopic", // useful to get the topic arn from the topic name
        "sns:Subscribe"
      ]
      Effect   = "Allow"
      Resource = ["arn:aws:sns:*:*:*"]
    }
  ]
}

module "shop_lambda" {
  source = "./api_lambda"

  lambda_dist_bucket = var.lambda_dist_bucket == "hot-reload" ? var.lambda_dist_bucket : aws_s3_bucket.lambda_bucket[0].bucket
  lambda_dist_bucket_key = var.shop_lambda_dist_bucket_key
  lambda_dist_path = var.shop_lambda_dist_path
  webapp_db_arn = module.webapp_db.arn
  is_testing = var.is_testing
  is_observability_enabled = var.is_observability_enabled
  lambda_system_properties = {
    logging_level = "INFO"
    spring_active_profile = var.shop_lambda_spring_active_profile
    spring_datasource_url = "jdbc:postgresql://${module.webapp_db.rds_endpoint}/${var.webapp_db_config.db_name}"
    spring_datasource_username = var.webapp_db_credentials.username
    spring_datasource_password = var.webapp_db_credentials.password
  }
  function_name = "shop-lambda"
  main_class = "it.unimi.cloudproject.ShopApi"
  extended_policy_statements = [
    {
      Action = [
        "sns:CreateTopic",
        "sns:DeleteTopic",
        "sns:Publish"
      ]
      Effect   = "Allow"
      Resource = ["arn:aws:sns:*:*:*"]
    }
  ]
}

module "admin_lambda" {
  source = "./api_lambda"

  lambda_dist_bucket = var.lambda_dist_bucket == "hot-reload" ? var.lambda_dist_bucket : aws_s3_bucket.lambda_bucket[0].bucket
  lambda_dist_bucket_key = var.admin_lambda_dist_bucket_key
  lambda_dist_path = var.admin_lambda_dist_path
  webapp_db_arn = module.webapp_db.arn
  is_testing = var.is_testing
  is_observability_enabled = var.is_observability_enabled
  lambda_system_properties = {
    logging_level = "INFO"
    spring_active_profile = var.admin_lambda_spring_active_profile
    spring_datasource_url = "jdbc:postgresql://${module.webapp_db.rds_endpoint}/${var.webapp_db_config.db_name}"
    spring_datasource_username = var.webapp_db_credentials.username
    spring_datasource_password = var.webapp_db_credentials.password
  }
  lambda_additional_system_properties = <<EOT
    -Daws.cognito.user_pool_id=${module.authentication.cognito_main_pool_id}
  EOT
  function_name = "admin-lambda"
  main_class = "it.unimi.cloudproject.AdminApi"
  extended_policy_statements = [
    {
      Action = [
        "cognito-idp:AdminAddUserToGroup"
      ]
      Effect   = "Allow"
      Resource = ["*"]
    },
    {
      Action = [
        "sns:CreateTopic"
      ]
      Effect   = "Allow"
      Resource = ["arn:aws:sns:*:*:*"]
    }
  ]
}

module "authorizer_lambda" {
  source = "./api_lambda"

  lambda_dist_bucket = var.lambda_dist_bucket == "hot-reload" ? var.lambda_dist_bucket : aws_s3_bucket.lambda_bucket[0].bucket
  lambda_dist_bucket_key = var.authorizer_lambda_dist_bucket_key
  lambda_dist_path = var.authorizer_lambda_dist_path
  webapp_db_arn = module.webapp_db.arn
  is_testing = var.is_testing
  // TODO: disabled here because the X-Ray Daemon seems to not work properly
  is_observability_enabled = false // var.is_observability_enabled
  lambda_system_properties = {
    logging_level = "INFO"
    spring_active_profile = var.authorizer_lambda_spring_active_profile
    spring_datasource_url = "jdbc:postgresql://${module.webapp_db.rds_endpoint}/${var.webapp_db_config.db_name}"
    spring_datasource_username = var.webapp_db_credentials.username
    spring_datasource_password = var.webapp_db_credentials.password
  }
  lambda_additional_system_properties = <<EOT
    -Daws.cognito.user_pool_id=${module.authentication.cognito_main_pool_id}
  EOT
  function_name = "authorizer-lambda"
  main_class = "it.unimi.cloudproject.ApiGatewayAuthorizer"
  extended_policy_statements = [
    {
      Action = [
        "cognito-idp:AdminListGroupsForUser",
        "iam:GetRolePolicy",
        "iam:ListRolePolicies"
      ]
      Effect   = "Allow"
      Resource = ["*"]
    }
  ]
}

module "webapp_apigw" {
  source = "./webapp_apigw"

  customer_lambda_info = {
    invoke_arn = module.customer_lambda.invoke_arn
    function_name = module.customer_lambda.function_name
    lambda_arn = module.customer_lambda.lambda_arn
  }
  shop_lambda_info = {
    invoke_arn = module.shop_lambda.invoke_arn
    function_name = module.shop_lambda.function_name
    lambda_arn = module.shop_lambda.lambda_arn
  }
  admin_lambda_info = {
    invoke_arn = module.admin_lambda.invoke_arn
    function_name = module.admin_lambda.function_name
    lambda_arn = module.admin_lambda.lambda_arn
  }
  authorizer_lambda_info = {
    invoke_arn = module.authorizer_lambda.invoke_arn
    function_name = module.authorizer_lambda.function_name
    lambda_arn = module.authorizer_lambda.lambda_arn
  }
  cognito_user_pool_arn = module.authentication.cognito_main_pool_arn
}

resource "aws_api_gateway_deployment" "apigw_deployment" {
  depends_on = [module.webapp_apigw, module.webapp_db]

  rest_api_id = module.webapp_apigw.webapp_apigw_rest_api_id
  stage_name  = var.apigateway_stage_name
}