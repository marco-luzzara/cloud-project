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