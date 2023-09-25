variable "webapp_lambda_iam_role_arn" {
  description = "Role Arn for the webapp lambda"
  type        = string
  sensitive   = true
}

variable "webapp_lambda_spring_active_profile" {
  description = "Spring active profile for the webapp lambda"
  type        = string
}

variable "webapp_lambda_dist_path" {
  description = "Path of the distribution zip of the web app lambda"
  type        = string
}

variable "webapp_lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the web app lambda"
  type        = string
  default = "lambda-dist-bucket"
}

variable "webapp_lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the web app lambda"
  type        = string
  default = "dist.zip"
}

variable "webapp_lambda_disable_cert_checking" {
  description = "It is used to disable the SSL certificate checking when doing HTTPS request to the mocked AWS endpoints"
  type = bool
  default = false
}

variable "apigateway_stage_name" {
  description = "The stage name for the api gateway"
  type    = string
}

variable "webapp_db_config" {
  description = "The webapp db configuration"
  type    = object({
    port = number
    allocated_storage = number
    instance_class = string
    skip_final_snapshot = bool
    db_name = string
  })
}

variable "webapp_db_credentials" {
  description = "The webapp db credentials"
  type        = object({
    username = string
    password = string
  })
  sensitive = true
}

variable "is_testing" {
  description = "The application is in testing mode, this disables SSL certificates checking, for instance"
  type = bool
  default = false
}

variable "admin_user_credentials" {
  description = "Credentials for the admin user"
  type = object({
    username = string
    password = string
  })
  sensitive = true
}
