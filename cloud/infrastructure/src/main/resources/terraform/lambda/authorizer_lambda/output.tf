output "invoke_arn" {
  value = aws_lambda_function.authorizer_lambda.invoke_arn
}

output "function_name" {
  value = aws_lambda_function.authorizer_lambda.function_name
}

output "lambda_arn" {
  value = aws_lambda_function.authorizer_lambda.arn
}