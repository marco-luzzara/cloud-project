variable "webapp_db_arn" {
  description = "Db Arn used by the IAM role to allow the lambda to connect to it"
  type        = string
}

variable "function_name" {
  description = "the function name of the lambda"
  type = string
}

variable "main_class" {
  description = "The main class (fully qualified) having the @SpringBootApplication annotation"
  type = string
}

variable "lambda_system_properties" {
  description = "Spring active profile for the lambda"
  type        = object({
    logging_level = string
    spring_active_profile = string
    spring_datasource_url = string
    spring_datasource_username = string
    spring_datasource_password = string
  })
}

variable "lambda_additional_system_properties" {
  description = "Additional system properties for lambda function"
  type = string
  default = ""
}

variable "lambda_dist_path" {
  description = "Path of the distribution zip of the lambda"
  type        = string
}

variable "lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the lambda"
  type        = string
}

variable "lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the lambda"
  type        = string
}

variable "is_testing" {
  description = "The application is in testing mode, this disables SSL certificates checking, for instance"
  type = bool
}

variable "is_observability_enabled" {
  description = "Flag to enable the observability using the OpenTelemetry collector"
  type = bool
}

variable "extended_policy_statements" {
  description = "policy statements to extend the basic policy of the api lambda"
  type = list(object({
    Action = list(string)
    Effect = string,
    Resource = list(string)
  }))
}

variable "additional_layers" {
  description = "Additional custom layers for the lambda function"
  type = list(string)
  default = []
}