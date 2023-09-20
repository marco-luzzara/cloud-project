resource "aws_s3_bucket" "webapp_lambda_bucket" {
  count = var.webapp_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = var.webapp_lambda_dist_bucket
}

resource "aws_s3_object" "webapp_lambda_distribution_zip" {
  count = var.webapp_lambda_dist_bucket == "hot-reload" ? 0 : 1

  bucket = aws_s3_bucket.webapp_lambda_bucket[count.index].bucket
  key    = var.webapp_lambda_dist_bucket_key
  source = var.webapp_lambda_dist_path
}

resource "aws_lambda_function" "webapp" {
  depends_on = [aws_s3_object.webapp_lambda_distribution_zip]
  function_name = "webapp"
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = var.webapp_lambda_iam_role_arn
  timeout      = 900

  environment {
    variables = {
      FUNCTION_INVOKER_LATE_INITIALIZATION = "true"
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=it.unimi.cloudproject.CloudProjectApplication
        -Dlogging.level.org.springframework=INFO
        -Daws.cognito.user_pool_client_id=${var.webapp_lambda_system_properties.cognito_main_user_pool_client_id}
        -Dspring.profiles.active=${var.webapp_lambda_system_properties.spring_active_profile}
        -Dspring.datasource.url=${var.webapp_lambda_system_properties.spring_datasource_url}
        -Dspring.datasource.username=${var.webapp_lambda_system_properties.spring_datasource_username}
        -Dspring.datasource.password=${var.webapp_lambda_system_properties.spring_datasource_password}
      EOT
    }
  }

  s3_bucket = var.webapp_lambda_dist_bucket
  s3_key = var.webapp_lambda_dist_bucket_key
  source_code_hash = var.webapp_lambda_dist_bucket == "hot-reload" ? null : filebase64sha256(var.webapp_lambda_dist_path)
}

#resource "null_resource" "wait_for_webapp_active" {
#  depends_on = [aws_lambda_function.webapp]
#
#  triggers = {
#    lambda_arn = aws_lambda_function.webapp.arn
#  }
#
#  provisioner "local-exec" {
#    command = <<EOT
#      aws lambda wait function-active-v2 --function-name "${aws_lambda_function.webapp.function_name}"
#    EOT
#  }
#}