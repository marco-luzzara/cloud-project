variable "webapp_db_arn" {
  description = "Db Arn used by the IAM role to allow the lambda to connect to it"
  type        = string
}

variable "initializer_lambda_system_properties" {
  description = "Spring active profile for the webapp lambda"
  type        = object({
    spring_active_profile = string
    spring_datasource_url = string
    spring_datasource_username = string
    spring_datasource_password = string
  })
}

variable "initializer_lambda_dist_path" {
  description = "Path of the distribution zip of the web app lambda"
  type        = string
}

variable "initializer_lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the web app lambda"
  type        = string
}

variable "initializer_lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the web app lambda"
  type        = string
}