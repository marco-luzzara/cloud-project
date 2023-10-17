variable "localstack_network" {
  description = <<EOT
    The network id to which the localstack container is connected to. This is necessary because the ECS container
    has the LAMBDA_DOCKER_NETWORK env variable.
    EOT
  type = string
}

variable "is_testing" {
  description = "Used to enable required configuration imposed by Localstack"
  type = bool
}