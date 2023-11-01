locals {
  policy = {
    Version   = "2012-10-17",
    Statement = concat([
      {
        Action = [
          "rds-db:connect"
        ],
        Effect   = "Allow",
        Resource = var.webapp_db_arn,
      },
      {
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Effect   = "Allow",
        Resource = "arn:aws:logs:*:*:*",
      },
      {
        Action = [
          "xray:PutTraceSegments",
          "xray:PutTelemetryRecords"
        ],
        Effect   = "Allow",
        Resource = "*",
      }
    ], var.extended_policy_statements)
  }
}

resource "aws_s3_bucket" "lambda_bucket" {
  count = var.lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = var.lambda_dist_bucket
}

resource "aws_s3_object" "lambda_distribution_zip" {
  count = var.lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = aws_s3_bucket.lambda_bucket[count.index].bucket
  key    = var.lambda_dist_bucket_key
  source = var.lambda_dist_path
}

resource "aws_iam_role" "lambda_role" {
  name               = "${var.function_name}-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "lambda_role_policy" {
  name = "${var.function_name}-policy"
  policy = jsonencode(local.policy)
  role = aws_iam_role.lambda_role.id
}

resource "aws_lambda_function" "api_lambda" {
  depends_on = [aws_s3_object.lambda_distribution_zip]
  function_name = var.function_name
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = aws_iam_role.lambda_role.arn
  timeout      = 900

  layers = var.additional_layers

  tracing_config {
    mode = "Active"
  }

  environment {
    variables = {
      LAMBDA_DOCKER_DNS = "127.0.0.1"
#      OTEL_RESOURCE_ATTRIBUTES = "service.name=${var.function_name},service.namespace=cloud-project"
#      OTEL_TRACES_EXPORTER = "otlp"
#      OTEL_EXPORTER_OTLP_ENDPOINT = "http://localhost:4566"
      OTEL_SERVICE_NAME = var.function_name
      OTEL_TRACES_EXPORTER = "logging"
      OTEL_METRICS_EXPORTER = "logging"
      OTEL_LOGS_EXPORTER = "logging"
#      AWS_LAMBDA_EXEC_WRAPPER = "/opt/otel-stream-handler"
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=${var.main_class}
        -Dlogging.level.org.springframework=${var.lambda_system_properties.logging_level}
        ${var.lambda_additional_system_properties}
        -Dspring.profiles.active=${var.lambda_system_properties.spring_active_profile}
        -Dspring.datasource.url=${var.lambda_system_properties.spring_datasource_url}
        -Dspring.datasource.username=${var.lambda_system_properties.spring_datasource_username}
        -Dspring.datasource.password=${var.lambda_system_properties.spring_datasource_password}
        ${var.is_testing ? "-javaagent:/var/task/lib/AwsSdkV2DisableCertificateValidation-1.0.jar" : ""}
      EOT
    }
  }

  s3_bucket = var.lambda_dist_bucket
  s3_key = var.lambda_dist_bucket_key
}