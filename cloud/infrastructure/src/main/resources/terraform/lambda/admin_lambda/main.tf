resource "aws_s3_bucket" "admin_lambda_bucket" {
  count = var.admin_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = var.admin_lambda_dist_bucket
}

resource "aws_s3_object" "admin_lambda_distribution_zip" {
  count = var.admin_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = aws_s3_bucket.admin_lambda_bucket[count.index].bucket
  key    = var.admin_lambda_dist_bucket_key
  source = var.admin_lambda_dist_path
}

data "aws_iam_policy_document" "admin_trust_policy" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "admin_lambda_role" {
  name               = "admin-lambda-role"
  assume_role_policy = data.aws_iam_policy_document.admin_trust_policy.json
}

resource "aws_iam_policy" "admin_lambda_policy" {
  name = "admin-lambda-policy"
  description = "IAM policy for admin lambda execution"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = [
        "rds-db:connect"
      ],
      Effect   = "Allow",
      Resource = var.webapp_db_arn,
    }]
  })
}

resource "aws_iam_role_policy_attachment" "admin_lambda_policy_attachment" {
  policy_arn = aws_iam_policy.admin_lambda_policy.arn
  role = aws_iam_role.admin_lambda_role.name
}

resource "aws_lambda_function" "admin_lambda" {
  depends_on = [aws_s3_object.admin_lambda_distribution_zip]
  function_name = "admin-lambda"
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = aws_iam_role.admin_lambda_role.arn
  timeout      = 900

  environment {
    variables = {
      LAMBDA_DOCKER_DNS = "127.0.0.1"
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=it.unimi.cloudproject.AdminApi
        -Dlogging.level.org.springframework=INFO
        -Daws.cognito.user_pool_id=${var.admin_lambda_system_properties.cognito_main_user_pool_id}
        -Daws.cognito.user_pool_client_id=${var.admin_lambda_system_properties.cognito_main_user_pool_client_id}
        -Daws.cognito.user_pool_client_secret=${var.admin_lambda_system_properties.cognito_main_user_pool_client_secret}
        -Dspring.profiles.active=${var.admin_lambda_system_properties.spring_active_profile}
        -Dspring.datasource.url=${var.admin_lambda_system_properties.spring_datasource_url}
        -Dspring.datasource.username=${var.admin_lambda_system_properties.spring_datasource_username}
        -Dspring.datasource.password=${var.admin_lambda_system_properties.spring_datasource_password}
        ${var.is_testing ? "-javaagent:/var/task/lib/AwsSdkV2DisableCertificateValidation-1.0.jar" : ""}
      EOT
    }
  }

  s3_bucket = var.admin_lambda_dist_bucket
  s3_key = var.admin_lambda_dist_bucket_key
}