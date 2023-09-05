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
  })
}

variable "webapp_lambda_dist_path" {
  description = "Path of the distribution zip of the web app lambda"
  type        = string
}
