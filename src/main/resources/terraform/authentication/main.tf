resource "aws_cognito_user_pool" "main_pool" {
  name = "main-pool"

  auto_verified_attributes = ["email"]
  username_attributes = ["email"]
  schema {
    attribute_data_type = "Number"
    mutable = false
    name = "dbId"
    required = true
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

resource "aws_cognito_user_group" "customer_user_group" {
  name         = "customer-user-group"
  user_pool_id = aws_cognito_user_pool.main_pool.id
}

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

resource "aws_cognito_user" "main_admin_user" {
  user_pool_id = aws_cognito_user_pool.main_pool.id
  username    = jsondecode(data.aws_secretsmanager_secret_version.admin_user_credentials_secret_data.secret_string)["username"]
  password    = jsondecode(data.aws_secretsmanager_secret_version.admin_user_credentials_secret_data.secret_string)["password"]
  attributes = {
    email = jsondecode(data.aws_secretsmanager_secret_version.admin_user_credentials_secret_data.secret_string)["username"]
  }
}

resource "aws_cognito_user_in_group" "main_admin_membership" {
  user_pool_id   = aws_cognito_user_pool.main_pool.id
  username       = aws_cognito_user.main_admin_user.username
  group_name     = aws_cognito_user_group.admin_user_group.name
}