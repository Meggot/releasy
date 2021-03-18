resource "aws_api_gateway_rest_api" "example" {
  name = var.app_name
  description = "Gateway for the ${var.app_name} Bot (v${var.app_version})"
}

output "base_url" {
  value = aws_api_gateway_deployment.example.invoke_url
}
