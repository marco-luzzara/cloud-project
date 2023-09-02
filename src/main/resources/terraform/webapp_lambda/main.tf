locals {
  lambda_dist_bucket = "lambda-dist-bucket"
  lambda_dist_bucket_key = "dist.zip"
}

resource "aws_s3_bucket" "webapp_lambda_bucket" {
  bucket = local.lambda_dist_bucket
}

resource "aws_s3_object" "webapp_lambda_distribution_zip" {
  bucket = local.lambda_dist_bucket
  key    = local.lambda_dist_bucket_key
  source = var.webapp_lambda_dist_path
}

resource "aws_lambda_function" "webapp" {
  function_name = "webapp"
  runtime      = "java17"
  handler      = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  role         = var.webapp_lambda_iam_role_arn
  timeout      = 900

  environment {
    variables = {
      JAVA_TOOL_OPTIONS = <<EOT
        -DMAIN_CLASS=it.unimi.cloudproject.CloudProjectApplication
        -Dlogging.level.org.springframework=INFO
        -Dspring.profiles.active=${var.webapp_lambda_spring_active_profile}
      EOT
    }
  }

  s3_bucket = local.lambda_dist_bucket
  s3_key = local.lambda_dist_bucket_key
}

resource "null_resource" "wait_for_webapp_active" {
  depends_on = [aws_lambda_function.webapp]

  triggers = {
    lambda_arn = aws_lambda_function.webapp.arn
  }

  provisioner "local-exec" {
    command = <<EOT
      aws lambda wait function-active-v2 --function-name "${aws_lambda_function.webapp.function_name}"
    EOT
  }
}