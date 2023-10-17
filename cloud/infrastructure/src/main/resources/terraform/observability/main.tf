resource "aws_iam_role" "ecs_prometheus_execution_role" {
  name = "ecs-execution-role"

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
          "cloudwatch:GetMetricData"
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
    host_path = "/app/cloudwatch_exporter_config.yml"
  }

  volume {
    name      = "prometheus-config-volume"
    host_path = "/app/prometheus.yml"
  }
}

resource "aws_ecs_service" "prometheus-service" {
  name            = "prometheus-service"
  cluster         = aws_ecs_cluster.prometheus_cluster.id
  task_definition = aws_ecs_task_definition.prometheus.arn
  launch_type     = "FARGATE"

  network_configuration {
    subnets = []
    assign_public_ip = false
  }

  desired_count = 1
}