// ******************************** Customer Lambda Variables

variable "customer_lambda_spring_active_profile" {
  description = "Spring active profile for the customer lambda"
  type        = string
}

variable "customer_lambda_dist_path" {
  description = "Path of the distribution zip of the customer lambda"
  type        = string
}

variable "customer_lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the customer lambda"
  type        = string
  default = "lambda-dist-bucket"
}

variable "customer_lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the customer lambda"
  type        = string
  default = "customer-api.zip"
}

// ******************************** Customer Lambda Variables

variable "authorizer_lambda_spring_active_profile" {
  description = "Spring active profile for the authorizer lambda"
  type        = string
}

variable "authorizer_lambda_dist_path" {
  description = "Path of the distribution zip of the authorizer lambda"
  type        = string
}

variable "authorizer_lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the authorizer lambda"
  type        = string
  default = "lambda-dist-bucket"
}

variable "authorizer_lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the authorizer lambda"
  type        = string
  default = "apigateway-authorizer.zip"
}

// ******************************** Shop Lambda Variables

variable "shop_lambda_spring_active_profile" {
  description = "Spring active profile for the shop lambda"
  type        = string
}

variable "shop_lambda_dist_path" {
  description = "Path of the distribution zip of the shop lambda"
  type        = string
}

variable "shop_lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the shop lambda"
  type        = string
  default = "lambda-dist-bucket"
}

variable "shop_lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the shop lambda"
  type        = string
  default = "shop-api.zip"
}

// ******************************** Admin Lambda Variables

variable "admin_lambda_spring_active_profile" {
  description = "Spring active profile for the admin lambda"
  type        = string
}

variable "admin_lambda_dist_path" {
  description = "Path of the distribution zip of the admin lambda"
  type        = string
}

variable "admin_lambda_dist_bucket" {
  description = "Bucket for the distribution zip of the admin lambda"
  type        = string
  default = "lambda-dist-bucket"
}

variable "admin_lambda_dist_bucket_key" {
  description = "Bucket key for the distribution zip of the admin lambda"
  type        = string
  default = "admin-api.zip"
}

// ********************************

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
