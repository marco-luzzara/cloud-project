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

resource "aws_lambda_function" "customer_lambda" {
  depends_on = [aws_s3_object.customer_lambda_distribution_zip]
  function_name = "customer-lambda"
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = var.customer_lambda_iam_role_arn
  timeout      = 900

  environment {
    variables = {
      LAMBDA_DOCKER_DNS = "127.0.0.1"
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=it.unimi.cloudproject.CustomerWebappApi
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