resource "aws_s3_bucket" "authorizer_lambda_bucket" {
  count = var.authorizer_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = var.authorizer_lambda_dist_bucket
}

resource "aws_s3_object" "authorizer_lambda_distribution_zip" {
  count = var.authorizer_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = aws_s3_bucket.authorizer_lambda_bucket[count.index].bucket
  key    = var.authorizer_lambda_dist_bucket_key
  source = var.authorizer_lambda_dist_path
}

resource "aws_iam_role" "authorizer_lambda_role" {
  name               = "authorizer-lambda-role"
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

resource "aws_iam_policy" "authorizer_lambda_policy" {
  name = "authorizer-lambda-policy"
  description = "IAM policy for authorizer lambda execution"

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
      },
      {
        Action = [
          "cognito-idp:AdminListGroupsForUser",
          "iam:GetRole"
        ],
        Effect   = "Allow",
        Resource = "*",
      },
    ]
  })
}

resource "aws_iam_role_policy_attachment" "authorizer_lambda_policy_attachment" {
  policy_arn = aws_iam_policy.authorizer_lambda_policy.arn
  role = aws_iam_role.authorizer_lambda_role.name
}

resource "aws_lambda_function" "authorizer_lambda" {
  depends_on = [aws_s3_object.authorizer_lambda_distribution_zip]
  function_name = "authorizer-lambda"
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = aws_iam_role.authorizer_lambda_role.arn
  timeout      = 900

  environment {
    variables = {
      LAMBDA_DOCKER_DNS = "127.0.0.1"
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=it.unimi.cloudproject.ApiGatewayAuthorizer
        -Dlogging.level.org.springframework=INFO
        -Daws.cognito.user_pool_id=${var.authorizer_lambda_system_properties.cognito_main_user_pool_id}
        -Daws.cognito.user_pool_client_id=${var.authorizer_lambda_system_properties.cognito_main_user_pool_client_id}
        -Daws.cognito.user_pool_client_secret=${var.authorizer_lambda_system_properties.cognito_main_user_pool_client_secret}
        -Dspring.profiles.active=${var.authorizer_lambda_system_properties.spring_active_profile}
        -Dspring.datasource.url=${var.authorizer_lambda_system_properties.spring_datasource_url}
        -Dspring.datasource.username=${var.authorizer_lambda_system_properties.spring_datasource_username}
        -Dspring.datasource.password=${var.authorizer_lambda_system_properties.spring_datasource_password}
        ${var.is_testing ? "-javaagent:/var/task/lib/AwsSdkV2DisableCertificateValidation-1.0.jar" : ""}
      EOT
    }
  }

  s3_bucket = var.authorizer_lambda_dist_bucket
  s3_key = var.authorizer_lambda_dist_bucket_key
}