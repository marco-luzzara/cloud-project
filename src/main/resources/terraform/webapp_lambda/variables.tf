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
