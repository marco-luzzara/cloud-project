resource "aws_s3_bucket" "shop_lambda_bucket" {
  count = var.shop_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = var.shop_lambda_dist_bucket
}

resource "aws_s3_object" "shop_lambda_distribution_zip" {
  count = var.shop_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = aws_s3_bucket.shop_lambda_bucket[count.index].bucket
  key    = var.shop_lambda_dist_bucket_key
  source = var.shop_lambda_dist_path
}

resource "aws_iam_role" "shop_lambda_role" {
  name               = "shop-lambda-role"
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

resource "aws_iam_policy" "shop_lambda_policy" {
  name = "shop-lambda-policy"
  description = "IAM policy for shop lambda execution"

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
          "sns:CreateTopic",
          "sns:DeleteTopic",
          "sns:Publish"
        ],
        Effect   = "Allow",
        Resource = "arn:aws:sns:*:*:*",
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "shop_lambda_policy_attachment" {
  policy_arn = aws_iam_policy.shop_lambda_policy.arn
  role = aws_iam_role.shop_lambda_role.name
}

resource "aws_lambda_function" "shop_lambda" {
  depends_on = [aws_s3_object.shop_lambda_distribution_zip]
  function_name = "shop-lambda"
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = aws_iam_role.shop_lambda_role.arn
  timeout      = 900

  environment {
    variables = {
      LAMBDA_DOCKER_DNS = "127.0.0.1"
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=it.unimi.cloudproject.ShopApi
        -Dlogging.level.org.springframework=INFO
        -Daws.cognito.user_pool_id=${var.shop_lambda_system_properties.cognito_main_user_pool_id}
        -Daws.cognito.user_pool_client_id=${var.shop_lambda_system_properties.cognito_main_user_pool_client_id}
        -Daws.cognito.user_pool_client_secret=${var.shop_lambda_system_properties.cognito_main_user_pool_client_secret}
        -Dspring.profiles.active=${var.shop_lambda_system_properties.spring_active_profile}
        -Dspring.datasource.url=${var.shop_lambda_system_properties.spring_datasource_url}
        -Dspring.datasource.username=${var.shop_lambda_system_properties.spring_datasource_username}
        -Dspring.datasource.password=${var.shop_lambda_system_properties.spring_datasource_password}
        ${var.is_testing ? "-javaagent:/var/task/lib/AwsSdkV2DisableCertificateValidation-1.0.jar" : ""}
      EOT
    }
  }

  s3_bucket = var.shop_lambda_dist_bucket
  s3_key = var.shop_lambda_dist_bucket_key
}