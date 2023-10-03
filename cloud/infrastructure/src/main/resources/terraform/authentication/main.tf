data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

locals {
  aws_account_id    = data.aws_caller_identity.current.account_id
  aws_region        = data.aws_region.current.name
  api_resource_prefix = "arn:aws:execute-api:${local.aws_region}:${local.aws_account_id}:*/*"
}

resource "aws_cognito_user_pool" "main_pool" {
  name = "main-pool"

  auto_verified_attributes = ["email"]
  username_attributes = ["email"]
  schema {
    attribute_data_type = "Number"
    mutable = false
    name = "dbId"
    number_attribute_constraints {
      min_value = 1
    }
  }
  password_policy {
    minimum_length    = 8
    require_lowercase = false
    require_numbers   = false
    require_symbols   = false
    require_uppercase = false
  }
  user_pool_add_ons {
    advanced_security_mode = "OFF"
  }
}

#resource "aws_iam_role" "cognito_customer_user_group_role" {
#  name = "cognito-group-role"
#  assume_role_policy = jsonencode({
#    Version = "2012-10-17"
#    Statement = [
#      {
#        Action = "execute-api:Invoke"
#        Effect   = "Allow"
#        Resource = "${local.api_resource_prefix}/GET/users/me"
#      },
#      {
#        Action = "execute-api:Invoke"
#        Effect   = "Deny"
#        Resource = "${local.api_resource_prefix}/DELETE/users/me"
#      }
#    ]
#  })
#}

resource "aws_cognito_user_group" "customer_user_group" {
  name         = "customer-user-group"
  user_pool_id = aws_cognito_user_pool.main_pool.id
#  role_arn = aws_iam_role.cognito_customer_user_group_role.arn
}

#resource "aws_iam_role" "cognito_customer_user_group_role" {
#  name = "cognito-group-role"
#
#  assume_role_policy = jsonencode({
#    Version = "2012-10-17",
#    Statement = [
#      {
#        Action = "sts:AssumeRoleWithWebIdentity",
#        Effect = "Allow",
#        Principal = {
#          Federated = "cognito-identity.amazonaws.com"
#        },
#        Condition = {
#          StringEquals = {
#            "cognito-identity.amazonaws.com:aud" = aws_cognito_user_pool.example.id
#          }
#        }
#      }
#    ]
#  })
#}

resource "aws_cognito_user_group" "shop_user_group" {
  name         = "shop-user-group"
  user_pool_id = aws_cognito_user_pool.main_pool.id
}

resource "aws_cognito_user_group" "admin_user_group" {
  name         = "admin-user-group"
  user_pool_id = aws_cognito_user_pool.main_pool.id
}

resource "aws_cognito_user_pool_client" "main_pool_client" {
  name            = "main-pool-client"
  user_pool_id    = aws_cognito_user_pool.main_pool.id
  allowed_oauth_flows_user_pool_client = true
  callback_urls = ["https://example.com"]
  allowed_oauth_flows                 = ["implicit"]
  allowed_oauth_scopes                = ["openid", "email", "profile"]

  generate_secret = true
}

// create admin user. This special user is in the admin group and can promote other users

resource "aws_secretsmanager_secret" "admin_user_credentials_secret" {
  description = "Admin user credentials"
  recovery_window_in_days = 0 // Overriding the default recovery window of 30 days, so that it can be immediately deleted
}

resource "aws_secretsmanager_secret_version" "admin_user_credentials_secret_value" {
  secret_id     = aws_secretsmanager_secret.admin_user_credentials_secret.id
  secret_string = jsonencode(var.admin_user_credentials)
}

data "aws_secretsmanager_secret_version" "admin_user_credentials_secret_data" {
  depends_on = [aws_secretsmanager_secret_version.admin_user_credentials_secret_value]
  secret_id     = aws_secretsmanager_secret.admin_user_credentials_secret.id
}

# TODO: https://github.com/hashicorp/terraform/issues/33943
#resource "aws_cognito_user" "main_admin_user" {
#  user_pool_id = aws_cognito_user_pool.main_pool.id
#  username    = jsondecode(data.aws_secretsmanager_secret_version.admin_user_credentials_secret_data.secret_string)["username"]
#  password    = jsondecode(data.aws_secretsmanager_secret_version.admin_user_credentials_secret_data.secret_string)["password"]
#  attributes = {
#    email = jsondecode(data.aws_secretsmanager_secret_version.admin_user_credentials_secret_data.secret_string)["username"]
#  }
#}

resource "aws_cognito_user" "main_admin_user" {
  user_pool_id = aws_cognito_user_pool.main_pool.id
  username    = var.admin_user_credentials.username
  password    = var.admin_user_credentials.password
  attributes = {
    email = var.admin_user_credentials.username
  }
}

resource "aws_cognito_user_in_group" "main_admin_membership" {
  user_pool_id   = aws_cognito_user_pool.main_pool.id
  username       = aws_cognito_user.main_admin_user.username
  group_name     = aws_cognito_user_group.admin_user_group.name
}