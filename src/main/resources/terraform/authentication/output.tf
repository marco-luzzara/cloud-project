output "cognito_main_pool_id" {
  value = aws_cognito_user_pool.main_pool.id
}

output "cognito_main_pool_client_id" {
  value = aws_cognito_user_pool_client.main_pool_client.id
}
