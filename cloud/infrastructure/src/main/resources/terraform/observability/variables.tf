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

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  type    = string
  default = "10.0.2.0/24"
}

variable "prometheus_config_host_path" {
  description = "The host path for the prometheus config file"
  type = string
}

variable "prometheus_exporter_config_host_path" {
  description = "The host path for the prometheus exporter (for Cloudwatch) config file"
  type = string
}