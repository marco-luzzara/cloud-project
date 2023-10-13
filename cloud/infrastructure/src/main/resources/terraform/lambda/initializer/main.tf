resource "aws_s3_bucket" "initializer_lambda_bucket" {
  count = var.initializer_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = var.initializer_lambda_dist_bucket
}

resource "aws_s3_object" "initializer_lambda_distribution_zip" {
  count = var.initializer_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = aws_s3_bucket.initializer_lambda_bucket[count.index].bucket
  key    = var.initializer_lambda_dist_bucket_key
  source = var.initializer_lambda_dist_path
}

resource "aws_iam_role" "initializer_lambda_role" {
  name               = "initializer-lambda-role"
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

resource "aws_iam_policy" "initializer_lambda_policy" {
  name = "initializer-lambda-policy"
  description = "IAM policy for initializer lambda execution"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
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
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "initializer_lambda_policy_attachment" {
  policy_arn = aws_iam_policy.initializer_lambda_policy.arn
  role = aws_iam_role.initializer_lambda_role.name
}

resource "aws_lambda_function" "initializer_lambda" {
  depends_on = [aws_s3_object.initializer_lambda_distribution_zip]
  function_name = "initializer-lambda"
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = aws_iam_role.initializer_lambda_role.arn
  timeout      = 900

  environment {
    variables = {
      LAMBDA_DOCKER_DNS = "127.0.0.1"
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=it.unimi.cloudproject.Initializer
        -Dlogging.level.org.springframework=INFO
        -Dspring.profiles.active=${var.initializer_lambda_system_properties.spring_active_profile}
        -Dspring.datasource.url=${var.initializer_lambda_system_properties.spring_datasource_url}
        -Dspring.datasource.username=${var.initializer_lambda_system_properties.spring_datasource_username}
        -Dspring.datasource.password=${var.initializer_lambda_system_properties.spring_datasource_password}
      EOT
    }
  }

  s3_bucket = var.initializer_lambda_dist_bucket
  s3_key = var.initializer_lambda_dist_bucket_key
}