variable "customer_lambda_invoke_arn" {
  description = "The invoke arn of the customer lambda function"
  type    = string
}

variable "cognito_user_pool_arn" {
  description = "ARN of User pool client used to authenticate users"
  type    = string
}