resource "aws_s3_bucket" "customer_lambda_bucket" {
  count = var.customer_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = var.customer_lambda_dist_bucket
}

resource "aws_s3_object" "customer_lambda_distribution_zip" {
  count = var.customer_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = aws_s3_bucket.customer_lambda_bucket[count.index].bucket
  key    = var.customer_lambda_dist_bucket_key
  source = var.customer_lambda_dist_path
}

resource "aws_iam_role" "customer_lambda_role" {
  name               = "customer-lambda-role"
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

resource "aws_iam_policy" "customer_lambda_policy" {
  name = "customer-lambda-policy"
  description = "IAM policy for customer lambda execution"

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
          "cognito-idp:AdminConfirmSignUp",
          "cognito-idp:SignUp",
          "cognito-idp:AdminDeleteUser",
          "cognito-idp:AdminAddUserToGroup"
        ],
        Effect   = "Allow",
        Resource = "*",
      },
      {
        Action = [
          "sns:CreateTopic", // useful to get the topic arn from the topic name
          "sns:Subscribe"
        ],
        Effect   = "Allow",
        Resource = "arn:aws:sns:*:*:*",
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "customer_lambda_policy_attachment" {
  policy_arn = aws_iam_policy.customer_lambda_policy.arn
  role = aws_iam_role.customer_lambda_role.name
}

resource "aws_lambda_function" "customer_lambda" {
  depends_on = [aws_s3_object.customer_lambda_distribution_zip]
  function_name = "customer-lambda"
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = aws_iam_role.customer_lambda_role.arn
  timeout      = 900

  environment {
    variables = {
      LAMBDA_DOCKER_DNS = "127.0.0.1"
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=it.unimi.cloudproject.CustomerApi
        -Dlogging.level.org.springframework=INFO
        -Daws.cognito.user_pool_id=${var.customer_lambda_system_properties.cognito_main_user_pool_id}
        -Daws.cognito.user_pool_client_id=${var.customer_lambda_system_properties.cognito_main_user_pool_client_id}
        -Daws.cognito.user_pool_client_secret=${var.customer_lambda_system_properties.cognito_main_user_pool_client_secret}
        -Dspring.profiles.active=${var.customer_lambda_system_properties.spring_active_profile}
        -Dspring.datasource.url=${var.customer_lambda_system_properties.spring_datasource_url}
        -Dspring.datasource.username=${var.customer_lambda_system_properties.spring_datasource_username}
        -Dspring.datasource.password=${var.customer_lambda_system_properties.spring_datasource_password}
        ${var.is_testing ? "-javaagent:/var/task/lib/AwsSdkV2DisableCertificateValidation-1.0.jar" : ""}
      EOT
    }
  }

  s3_bucket = var.customer_lambda_dist_bucket
  s3_key = var.customer_lambda_dist_bucket_key
}