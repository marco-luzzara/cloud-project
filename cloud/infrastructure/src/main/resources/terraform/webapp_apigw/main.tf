locals {
  request_mapping_template = <<-EOT
    {
      "headers": {
        #foreach($param in $input.params().header.keySet())
          "$param": "$util.escapeJavaScript($input.params().header.get($param))"
          #if($foreach.hasNext),#end
        #end
      },
      "body" : %s
    }
    EOT
}

resource "aws_api_gateway_rest_api" "webapp_rest_api" {
  name = "webapp-api"
}

#resource "aws_iam_role" "apigw_role" {
#  name               = "apigw-role"
#  assume_role_policy = jsonencode({
#    Version = "2012-10-17",
#    Statement = [
#      {
#        Action = "sts:AssumeRole",
#        Effect = "Allow",
#        Principal = {
#          Service = "apigateway.amazonaws.com"
#        }
#      }
#    ]
#  })
#}
#
#resource "aws_iam_policy" "apigw_policy" {
#  name = "apigw-policy"
#  description = "IAM policy for the Api Gateway"
#
#  policy = jsonencode({
#    Version = "2012-10-17",
#    Statement = [{
#      Action = [
#        "lambda:InvokeFunction"
#      ],
#      Effect   = "Allow",
#      Resource = "*"
##      Resource = [
##        var.customer_lambda_info.lambda_arn,
##        var.admin_lambda_info.lambda_arn
##      ]
#    }]
#  })
#}
#
#resource "aws_iam_role_policy_attachment" "apigw_policy_attachment" {
#  policy_arn = aws_iam_policy.apigw_policy.arn
#  role = aws_iam_role.apigw_role.name
#}

#resource "aws_lambda_permission" "lambda_permission" {
#  for_each = toset([
#    var.customer_lambda_info.function_name,
#    var.admin_lambda_info.function_name
#  ])
#  statement_id  = "ApiGatewayInvoke${each.key}"
#  action        = "lambda:InvokeFunction"
#  function_name = each.key
#  principal     = "apigateway.amazonaws.com"
#  source_arn = "${aws_api_gateway_rest_api.webapp_rest_api.execution_arn}/*/*"
#}

resource "aws_lambda_permission" "lambda_permission" {
  statement_id  = "AllowApiGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = var.customer_lambda_info.function_name
  principal     = "apigateway.amazonaws.com"
#  source_arn = "${aws_api_gateway_rest_api.webapp_rest_api.execution_arn}/*/*"
}

resource "aws_api_gateway_authorizer" "user_authorizer" {
  name                   = "user-authorizer"
  rest_api_id             = aws_api_gateway_rest_api.webapp_rest_api.id
  type                   = "COGNITO_USER_POOLS"
  provider_arns           = [var.cognito_user_pool_arn]
  identity_source        = "method.request.header.Authorization"
}

# ************************ Users API ************************

# ********* POST /users
resource "aws_api_gateway_resource" "webapp_users_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_rest_api.webapp_rest_api.root_resource_id
  path_part   = "users"
}

module "create_user" {
  source = "../webapp_apigw_integration"
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_resource.id
  http_method = "POST"
  authorization = "NONE"
  authorizer_id = null
  lambda_invocation_arn = var.customer_lambda_info.invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = "$input.json('$')"
  spring_cloud_function_definition_header_value = "createUser"
}

# ********* POST /login
resource "aws_api_gateway_resource" "webapp_login_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_rest_api.webapp_rest_api.root_resource_id
  path_part   = "login"
}

module "user_login" {
  source = "../webapp_apigw_integration"
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_login_resource.id
  http_method = "POST"
  authorization = "NONE"
  authorizer_id = null
  lambda_invocation_arn = var.customer_lambda_info.invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = "$input.json('$')"
  spring_cloud_function_definition_header_value = "loginUser"
}

# ********* GET /users/me
resource "aws_api_gateway_resource" "webapp_users_me_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_resource.webapp_users_resource.id
  path_part   = "me"
}

module "get_user" {
  source = "../webapp_apigw_integration"
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_me_resource.id
  http_method = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.user_authorizer.id
  lambda_invocation_arn = var.customer_lambda_info.invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = <<-EOT
    {
      "userId": "$context.authorizer.claims['custom:dbId']"
    }
    EOT
  spring_cloud_function_definition_header_value = "getUser"
}

# ********* DELETE /users/me

module "delete_user" {
  source = "../webapp_apigw_integration"
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_me_resource.id
  http_method = "DELETE"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.user_authorizer.id
  lambda_invocation_arn = var.customer_lambda_info.invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = <<-EOT
    {
      "userId": "$context.authorizer.claims['custom:dbId']"
    }
    EOT
  spring_cloud_function_definition_header_value = "deleteUser"
}

# ********* POST /users/me/subscriptions/{shopId}

resource "aws_api_gateway_resource" "webapp_users_me_subscriptions_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_resource.webapp_users_me_resource.id
  path_part   = "subscriptions"
}

resource "aws_api_gateway_resource" "webapp_users_me_subscriptions_with_shopId_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_resource.webapp_users_me_subscriptions_resource.id
  path_part   = "{shopId}"
}

module "add_user_subscription" {
  source = "../webapp_apigw_integration"
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_me_subscriptions_with_shopId_resource.id
  http_method = "POST"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.user_authorizer.id
  lambda_invocation_arn = var.customer_lambda_info.invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = <<-EOT
    {
      "userId": "$context.authorizer.claims['custom:dbId']",
      "shopId": "$input.params('shopId')"
    }
    EOT
  spring_cloud_function_definition_header_value = "addShopSubscription"
  http_fail_status_codes = [
    {
      status_code = "404"
      selection_pattern = "shop with id \\d+ does not exist"
    }
  ]
}

## ************************ Shops API ************************

# ********* /shops
resource "aws_api_gateway_resource" "webapp_shops_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_rest_api.webapp_rest_api.root_resource_id
  path_part   = "shops"
}

module "create_shop" {
  source = "../webapp_apigw_integration"
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_shops_resource.id
  http_method = "POST"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.user_authorizer.id
  lambda_invocation_arn = var.admin_lambda_info.invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = "$input.json('$')"
  spring_cloud_function_definition_header_value = "createShop"
  http_fail_status_codes = [
    {
      status_code = "404"
      selection_pattern = "User with id \\d+ does not exist"
    }
  ]
}

# ********* /shops/{shopId}
resource "aws_api_gateway_resource" "webapp_shops_with_id_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_resource.webapp_shops_resource.id
  path_part   = "{shopId}"
}