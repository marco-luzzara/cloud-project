variable "rest_api_id" {
  description = "Rest API Id"
  type    = string
}

variable "resource_id" {
  description = "resource id to attach the method integration"
  type    = string
}

variable "http_method" {
  description = "http method used to call the api"
  type    = string
  validation {
    condition     = contains(["GET", "POST", "DELETE"], var.http_method)
    error_message = "Allowed values for http_method are \"GET\", \"POST\", \"DELETE\"."
  }
}

variable "authorization" {
  description = "The authorization type for the method"
  type = string
}

variable "authorizer_id" {
  description = "The authorizer id of the cognito client authorizer"
  type = string
}

variable "http_successful_status_code" {
  description = "http status code for the successful case"
  type    = string
  validation {
    condition     = can(regex("\\d{3}", var.http_successful_status_code))
    error_message = "Only 3 digits number are allowed"
  }
}

variable "http_fail_status_codes" {
  description = "http status codes for the failed cases, paired with the selection pattern to used to match them"
  type    = list(object({
    status_code = string
    selection_pattern = string
  }))
  default = []
}

variable "lambda_invocation_arn" {
  description = "arn of the lambda for its invocation"
  type    = string
}

variable "spring_cloud_function_definition_header_value" {
  description = "value to be assigned to the custom header X-Spring-Cloud-Function-Definition, used to populate the spring.cloud.function.definition"
  type    = string
}

variable "request_template_for_body" {
  description = "request template for the body passed to the integration"
  type    = string
}