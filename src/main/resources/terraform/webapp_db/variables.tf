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