variable "customer_lambda_info" {
  description = "The customer lambda info"
  type    = object({
    invoke_arn: string
    function_name: string
    lambda_arn: string
  })
}

variable "shop_lambda_info" {
  description = "The shop lambda info"
  type    = object({
    invoke_arn: string
    function_name: string
    lambda_arn: string
  })
}

variable "admin_lambda_info" {
  description = "The admin lambda info"
  type    = object({
    invoke_arn: string
    function_name: string
    lambda_arn: string
  })
}

variable "cognito_user_pool_arn" {
  description = "ARN of User pool client used to authenticate users"
  type    = string
}