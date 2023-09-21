variable "webapp_lambda_iam_role_arn" {
  description = "Role Arn for the webapp lambda"
  type        = string
  sensitive   = true
}

variable "webapp_lambda_system_properties" {
  description = "Spring active profile for the webapp lambda"
  type        = object({
    spring_active_profile = string
    spring_datasource_url = string
    spring_datasource_username = string
    spring_datasource_password = string
    cognito_main_user_pool_id = string
    cognito_main_user_pool_client_id = string
    cognito_main_user_pool_client_secret = string
    disable_cert_checking = bool
  })
}

variable "webapp_lambda_dist_path" {
  description = "Path of the distribution zip of the web app lambda"
  type        = string
}

variable "webapp_lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the web app lambda"
  type        = string
}

variable "webapp_lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the web app lambda"
  type        = string
}