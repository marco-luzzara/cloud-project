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
  lambda_invocation_arn = var.webapp_lambda_invoke_arn
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
  lambda_invocation_arn = var.webapp_lambda_invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = "$input.json('$')"
  spring_cloud_function_definition_header_value = "loginUser"
}

# ********* GET /users/me
resource "aws_api_gateway_resource" "webapp_users_with_id_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_resource.webapp_users_resource.id
  path_part   = "me"
}

module "get_user" {
  source = "../webapp_apigw_integration"
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.user_authorizer.id
  lambda_invocation_arn = var.webapp_lambda_invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = <<-EOT
    {
      "userId": "$context.authorizer.claims['custom:dbId']"
    }
    EOT
  spring_cloud_function_definition_header_value = "getUser"
  http_fail_status_codes = [
    {
      status_code = "404"
      selection_pattern = "user with id \\d+ does not exist"
    }
  ]
}

# ********* DELETE /users/{userId}

module "delete_user" {
  source = "../webapp_apigw_integration"
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method = "DELETE"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.user_authorizer.id
  lambda_invocation_arn = var.webapp_lambda_invoke_arn
  http_successful_status_code = "200"
  request_template_for_body = <<-EOT
    {
      "id": "$input.params('userId')"
    }
    EOT
  spring_cloud_function_definition_header_value = "deleteUser"
  http_fail_status_codes = [
    {
      status_code = "404"
      selection_pattern = "user with id \\d+ does not exist"
    }
  ]
}

## ************************ Shops API ************************
#
## ********* /shops
#resource "aws_api_gateway_resource" "webapp_shops_resource" {
#  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
#  parent_id   = aws_api_gateway_rest_api.webapp_rest_api.root_resource_id
#  path_part   = "shops"
#}
#
## ********* /shops/{shopId}
#resource "aws_api_gateway_resource" "webapp_shops_with_id_resource" {
#  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
#  parent_id   = aws_api_gateway_resource.webapp_shops_resource.id
#  path_part   = "{shopId}"
#}