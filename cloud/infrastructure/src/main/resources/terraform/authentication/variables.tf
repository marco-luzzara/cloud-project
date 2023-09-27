variable "admin_user_credentials" {
  description = "Credentials for the admin user"
  type = object({
    username = string
    password = string
  })
  sensitive = true
}