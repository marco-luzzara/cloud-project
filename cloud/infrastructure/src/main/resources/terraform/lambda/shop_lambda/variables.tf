variable "webapp_db_arn" {
  description = "Db Arn used by the IAM role to allow the lambda to connect to it"
  type        = string
}

variable "shop_lambda_system_properties" {
  description = "Spring active profile for the shop lambda"
  type        = object({
    spring_active_profile = string
    spring_datasource_url = string
    spring_datasource_username = string
    spring_datasource_password = string
    cognito_main_user_pool_id = string
    cognito_main_user_pool_client_id = string
    cognito_main_user_pool_client_secret = string
  })
}

variable "shop_lambda_dist_path" {
  description = "Path of the distribution zip of the web app lambda"
  type        = string
}

variable "shop_lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the web app lambda"
  type        = string
}

variable "shop_lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the web app lambda"
  type        = string
}

variable "is_testing" {
  description = "The application is in testing mode, this disables SSL certificates checking, for instance"
  type = bool
}