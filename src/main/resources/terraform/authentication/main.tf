resource "aws_cognito_user_pool" "main_pool" {
  name = "main-pool"

  auto_verified_attributes = ["email"]
  username_attributes = ["email"]
  schema {
    attribute_data_type = "Number"
    mutable = false
    name = "dbId"  # Custom attribute name
    required = true
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
  allowed_oauth_flows                 = ["client_credentials"]
  allowed_oauth_scopes                = []

  generate_secret = true
}