terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }

  required_version = ">= 1.2.0"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default = "us-east-1"
}

# provider in Localstack is overridden by provider_override.ts file because the
# endpoints must be manually set
provider "aws" {
  region = var.aws_region
}

module "webapp_lambda" {
  source = "./webapp_lambda"
  webapp_lambda_dist_path = var.webapp_lambda_dist_path
  webapp_lambda_iam_role_arn = var.webapp_lambda_iam_role_arn
  webapp_lambda_spring_active_profile = var.webapp_lambda_spring_active_profile

#  when = terraform.workspace == "webapp"
}

module "webapp_apigw" {
  source = "./webapp_lambda"
  webapp_lambda_dist_path = var.webapp_lambda_dist_path
  webapp_lambda_iam_role_arn = var.webapp_lambda_iam_role_arn
  webapp_lambda_spring_active_profile = var.webapp_lambda_spring_active_profile

  #  when = terraform.workspace == "webapp"
}