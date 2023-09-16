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
  api_data = [
    {

    }
  ]
}

resource "aws_api_gateway_rest_api" "webapp_rest_api" {
  name = "webapp-api"
}

# ************************ Users API ************************

# ********* POST /users
resource "aws_api_gateway_resource" "webapp_users_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_rest_api.webapp_rest_api.root_resource_id
  path_part   = "users"
}

resource "aws_api_gateway_method" "create_users" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_resource.id
  http_method   = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "create_users_response" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_resource.id
  http_method   = aws_api_gateway_method.create_users.http_method
  status_code = "200"
}

resource "aws_api_gateway_integration" "create_users_integration" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_resource.id
  http_method   = aws_api_gateway_method.create_users.http_method
  type                    = "AWS"
  integration_http_method = "POST"
  uri                     = var.webapp_lambda_invoke_arn
  passthrough_behavior    = "WHEN_NO_MATCH"

  request_parameters = {
    "integration.request.header.X-Spring-Cloud-Function-Definition" = "createUser"
  }

  request_templates = {
    "application/json" = format(local.request_mapping_template, "$input.json('$')")
  }
}

resource "aws_api_gateway_integration_response" "create_users_integration_response" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_resource.id
  http_method = aws_api_gateway_integration.create_users_integration.http_method
  status_code = aws_api_gateway_method_response.create_users_response.status_code
}

# ********* GET /users/{userId}
resource "aws_api_gateway_resource" "webapp_users_with_id_resource" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  parent_id   = aws_api_gateway_resource.webapp_users_resource.id
  path_part   = "{userId}"
}

resource "aws_api_gateway_method" "get_user" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "get_user_response_200" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method   = aws_api_gateway_method.get_user.http_method
  status_code = "200"
}

resource "aws_api_gateway_method_response" "get_user_response_404" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method   = aws_api_gateway_method.get_user.http_method
  status_code = "404"
}

resource "aws_api_gateway_integration" "get_user_integration" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method   = aws_api_gateway_method.get_user.http_method
  type                    = "AWS"
  integration_http_method = "POST"
  uri                     = var.webapp_lambda_invoke_arn
  passthrough_behavior    = "WHEN_NO_MATCH"

  request_parameters = {
    "integration.request.header.X-Spring-Cloud-Function-Definition" = "getUser"
  }

  request_templates = {
    "application/json" = format(local.request_mapping_template, <<-EOT
    {
      "id": "$input.params('userId')"
    }
    EOT
    )
  }
}

resource "aws_api_gateway_integration_response" "get_user_integration_response_200" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method = aws_api_gateway_integration.get_user_integration.http_method
  status_code = aws_api_gateway_method_response.get_user_response_200.status_code
}

resource "aws_api_gateway_integration_response" "get_user_integration_response_404" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method = aws_api_gateway_integration.get_user_integration.http_method
  status_code = aws_api_gateway_method_response.get_user_response_404.status_code
  selection_pattern = "user with id \\d+ does not exist"
}

# ********* DELETE /users/{userId}
resource "aws_api_gateway_method" "delete_user" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method   = "DELETE"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "delete_user_response_200" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method   = aws_api_gateway_method.delete_user.http_method
  status_code = "200"
}

resource "aws_api_gateway_method_response" "delete_user_response_404" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method   = aws_api_gateway_method.delete_user.http_method
  status_code = "404"
}

resource "aws_api_gateway_integration" "delete_user_integration" {
  rest_api_id   = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id   = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method   = aws_api_gateway_method.delete_user.http_method
  type                    = "AWS"
  integration_http_method = "POST"
  uri                     = var.webapp_lambda_invoke_arn
  passthrough_behavior    = "WHEN_NO_MATCH"

  request_parameters = {
    "integration.request.header.X-Spring-Cloud-Function-Definition" = "deleteUser"
  }

  request_templates = {
    "application/json" = format(local.request_mapping_template, <<-EOT
    {
      "id": "$input.params('userId')"
    }
    EOT
    )
  }
}

resource "aws_api_gateway_integration_response" "delete_user_integration_response_200" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method = aws_api_gateway_integration.delete_user_integration.http_method
  status_code = aws_api_gateway_method_response.delete_user_response_200.status_code
}

resource "aws_api_gateway_integration_response" "delete_user_integration_response_404" {
  rest_api_id = aws_api_gateway_rest_api.webapp_rest_api.id
  resource_id = aws_api_gateway_resource.webapp_users_with_id_resource.id
  http_method = aws_api_gateway_integration.delete_user_integration.http_method
  status_code = aws_api_gateway_method_response.delete_user_response_404.status_code
  selection_pattern = "user with id \\d+ does not exist"
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