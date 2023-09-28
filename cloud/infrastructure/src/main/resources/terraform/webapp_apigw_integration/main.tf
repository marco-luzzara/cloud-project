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

resource "aws_api_gateway_method" "api_method" {
  rest_api_id   = var.rest_api_id
  resource_id   = var.resource_id
  http_method   = var.http_method
  authorization = var.authorization
  authorizer_id = var.authorizer_id
}

resource "aws_api_gateway_method_response" "api_method_response_successful" {
  rest_api_id   = var.rest_api_id
  resource_id   = var.resource_id
  http_method   = aws_api_gateway_method.api_method.http_method
  status_code = var.http_successful_status_code
}

resource "aws_api_gateway_method_response" "api_method_response_fail" {
  for_each = { for idx, item in var.http_fail_status_codes : idx => item }

  rest_api_id   = var.rest_api_id
  resource_id   = var.resource_id
  http_method   = aws_api_gateway_method.api_method.http_method
  status_code   = each.value.status_code
}

resource "aws_api_gateway_integration" "api_integration" {
  rest_api_id   = var.rest_api_id
  resource_id   = var.resource_id
  http_method   = aws_api_gateway_method.api_method.http_method
  type                    = "AWS"
  integration_http_method = "POST"
  uri                     = var.lambda_invocation_arn
  passthrough_behavior    = "WHEN_NO_MATCH"

  request_parameters = {
    "integration.request.header.X-Spring-Cloud-Function-Definition" = var.spring_cloud_function_definition_header_value
  }

  request_templates = {
    "application/json" = format(local.request_mapping_template, var.request_template_for_body)
  }
}

resource "aws_api_gateway_integration_response" "api_integration_response_successful" {
  rest_api_id   = var.rest_api_id
  resource_id   = var.resource_id
  http_method = aws_api_gateway_integration.api_integration.http_method
  status_code = aws_api_gateway_method_response.api_method_response_successful.status_code
  response_parameters = {
    "method.response.header.Content-Type" = "application/json"
  }
}

resource "aws_api_gateway_integration_response" "api_integration_response_fail" {
  depends_on = [aws_api_gateway_method_response.api_method_response_fail]
  for_each = { for idx, item in var.http_fail_status_codes : idx => item }

  rest_api_id   = var.rest_api_id
  resource_id   = var.resource_id
  http_method = aws_api_gateway_integration.api_integration.http_method
  status_code = each.value.status_code
  selection_pattern = each.value.selection_pattern
}