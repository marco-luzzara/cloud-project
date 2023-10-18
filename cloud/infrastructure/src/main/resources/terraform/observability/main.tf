// ********************* networking configuration

resource "aws_vpc" "vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
}

resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = var.public_subnet_cidr
#  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = true
}

resource "aws_internet_gateway" "internet_gateway" {}

resource "aws_internet_gateway_attachment" "gateway_attachment" {
  vpc_id             = aws_vpc.vpc.id
  internet_gateway_id = aws_internet_gateway.internet_gateway.id
}

resource "aws_route_table" "public_route_table" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.internet_gateway.id
  }
}

resource "aws_route_table_association" "public_subnet" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.public_route_table.id
}

resource "aws_security_group" "fargate_container_security_group" {
  name        = "fargate-container-sg"
  description = "Access to the Fargate containers"
  vpc_id      = aws_vpc.vpc.id
}

/* TODO: Setting protocol = "all" or protocol = -1 with from_port and to_port
will result in the EC2 API creating a security group rule with all ports open.
This API behavior cannot be controlled by Terraform and may generate warnings in the future.
*/
resource "aws_security_group_rule" "ecs_security_group_ingress_from_self" {
  type        = "ingress"
  from_port   = 0
  to_port     = 65535
  protocol    = "-1"
  source_security_group_id = aws_security_group.fargate_container_security_group.id
  security_group_id        = aws_security_group.fargate_container_security_group.id
}

// ********************* prometheus configuration

resource "aws_iam_role" "ecs_prometheus_execution_role" {
  name = "ecs-tasks-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_policy" "prometheus_policy" {
  name = "prometheus-policy"
  description = "IAM policy for prometheus ECS service"

  policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Action = [
          "cloudwatch:ListMetrics",
          "cloudwatch:GetMetricStatistics",
          "cloudwatch:GetMetricData",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Effect   = "Allow"
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_prometheus_execution_role_attachment" {
  role      = aws_iam_role.ecs_prometheus_execution_role.name
  policy_arn = aws_iam_policy.prometheus_policy.arn
}

resource "aws_ecs_cluster" "prometheus_cluster" {
  name = "prometheus-cluster"
}

resource "aws_ecs_task_definition" "prometheus" {
  family = "prometheus-exporter"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.ecs_prometheus_execution_role.arn
  network_mode = "awsvpc"

  container_definitions = jsonencode([
    {
      name      = "prometheusexporter"
      image     = "prom/cloudwatch-exporter:v0.15.4"
      hostname  = "prometheusexporter"
      memory    = 256
      cpu       = 1
      essential = true
      // exposes 9106
      mountPoints = [
        {
          containerPath = "/config/config.yml"
          sourceVolume = "prometheus-exporter-config-volume"
        }
      ]
      environment = [
        {
          name = "LAMBDA_DOCKER_NETWORK"
          value = var.localstack_network
        }
      ]
    },
    {
      name      = "prometheus"
      image     = "prom/prometheus:v2.47.2"
      hostname  = "prometheus"
      memory    = 256
      cpu       = 1
      essential = true
      mountPoints = [
        {
          containerPath = "/etc/prometheus/prometheus.yml"
          sourceVolume = "prometheus-config-volume"
        }
      ]
      portMappings = [
        {
          containerPort = 9090
          hostPort      = 9090
        }
      ]
      environment = [
        {
          name = "LAMBDA_DOCKER_NETWORK"
          value = var.localstack_network
        }
      ]
    }
  ])

  volume {
    name      = "prometheus-exporter-config-volume"
    host_path = var.prometheus_exporter_config_host_path
  }

  volume {
    name      = "prometheus-config-volume"
    host_path = var.prometheus_config_host_path
  }
}

resource "aws_ecs_service" "prometheus-service" {
  name            = "prometheus-service"
  cluster         = aws_ecs_cluster.prometheus_cluster.id
  task_definition = aws_ecs_task_definition.prometheus.arn
  launch_type     = "FARGATE"

  network_configuration {
    subnets = [
      aws_subnet.public_subnet.id
    ]
    security_groups = [
      aws_security_group.fargate_container_security_group.id
    ]
    assign_public_ip = true
  }

  desired_count = 1
}