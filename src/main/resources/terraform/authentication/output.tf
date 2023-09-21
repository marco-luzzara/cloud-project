output "cognito_main_pool_id" {
  value = aws_cognito_user_pool.main_pool.id
}

output "cognito_main_pool_arn" {
  value = aws_cognito_user_pool.main_pool.arn
}

output "cognito_main_pool_client_id" {
  value = aws_cognito_user_pool_client.main_pool_client.id
}

output "cognito_main_pool_client_secret" {
  value = aws_cognito_user_pool_client.main_pool_client.client_secret
}