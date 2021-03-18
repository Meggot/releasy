data "aws_caller_identity" "current" {}

resource "aws_s3_bucket" "releasy_releases" {
  bucket = "releasy-releases"
  acl = "private"

  versioning {
    enabled = true
  }

  policy = <<-EOF
    {
      "Id": "Policy",
      "Version": "2012-10-17",
      "Statement": [
        {
          "Action": [
            "s3:PutObject"
          ],
          "Effect": "Allow",
          "Resource": "arn:aws:s3:::releasy-releases/alb-logs/*",
          "Principal": {
            "AWS": [
              "${data.aws_caller_identity.current.account_id}"
            ]
          }
        }
      ]
    }
    EOF
}

# UPLOAD TARGET
resource "aws_s3_bucket_object" "auth_lambda_code" {
  bucket = aws_s3_bucket.releasy_releases.bucket
  key = "releasy/releasy_auth_lambda_${var.app_version}.zip"
  source = "${var.target_dir}/releasy_auth_lambda_${var.app_version}.zip"
}


# CONFIGURE LAMBDA
resource "aws_lambda_function" "releasy_auth_lambda" {
  function_name = "releasy_auth_lambda"

  s3_bucket = aws_s3_bucket_object.auth_lambda_code.bucket
  s3_key = aws_s3_bucket_object.auth_lambda_code.key

  vpc_config {
    subnet_ids = module.vpc.private_subnets
    security_group_ids = [
      module.vpc.default_security_group_id,
      aws_security_group.releasy_lambda_sg.id]
  }

  handler = "lambda_function.lambda_handler"
  runtime = "python3.7"

  role = aws_iam_role.lambda_exec.arn

  environment {
    variables = {
      SLACK_SIGNING_SECRET = var.slack_signing_secret
    }
  }
}

resource "aws_dynamodb_table" "releasy_templates" {
  name = "releasy_templates"
  hash_key = "user_id_project_code"
  write_capacity = 20
  read_capacity = 20

  attribute {
    name = "user_id_project_code"
    type = "S"
  }
}

resource "aws_dynamodb_table" "releasy_releases" {
  name = "releasy_releases"
  hash_key = "release_title"
  write_capacity = 20
  read_capacity = 20

  attribute {
    name = "release_title"
    type = "S"
  }
}

resource "aws_s3_bucket_object" "main_lambda_code" {
  bucket = aws_s3_bucket.releasy_releases.bucket
  key = "releasy/releasy_main_lambda_${var.app_version}.zip"
  source = "${var.target_dir}/releasy_main_lambda_${var.app_version}.zip"
}

resource "aws_lambda_function" "releasy_main_lambda" {
  function_name = "releasy_main"

  s3_bucket = aws_s3_bucket_object.main_lambda_code.bucket
  s3_key = aws_s3_bucket_object.main_lambda_code.key

  vpc_config {
    subnet_ids = module.vpc.private_subnets
    security_group_ids = [
      module.vpc.default_security_group_id,
      aws_security_group.releasy_lambda_sg.id]
  }
  handler = "lambda_function.lambda_handler"
  runtime = "python3.7"
  timeout = 30

  role = aws_iam_role.lambda_exec.arn

  environment {
    variables = {
      RELEASY_URL = aws_lb.releasy_alb.dns_name
      SLACK_TOKEN = var.slack_token
    }
  }
}

resource "aws_security_group" "releasy_lambda_sg" {
  name = "releasy-main-lambda-sg"
  description = "Releasy Lambda"
  vpc_id = module.vpc.vpc_id

  ingress {
    # TLS (change to whatever ports you need)
    from_port = 0
    to_port = 65535
    protocol = "tcp"

    cidr_blocks = [
      "0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}

# IAM role which dictates what other AWS services the Lambda function
# may access.
resource "aws_iam_role" "lambda_exec" {
  name = "${var.app_name}_lambda"

  assume_role_policy = <<EOF
{
 "Version": "2012-10-17",
 "Statement": [
   {
     "Action": "sts:AssumeRole",
     "Principal": {
       "Service": "lambda.amazonaws.com"
     },
     "Effect": "Allow",
     "Sid": ""
   }
 ]
}
EOF
}

resource "aws_iam_role_policy" "releasy_table_access_policy" {
  name = "releasy_table_access_policy"
  role = "${aws_iam_role.lambda_exec.id}"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
            "Effect": "Allow",
            "Action": [
                "dynamodb:GetItem",
                "dynamodb:PutItem",
                "dynamodb:Scan",
                "dynamodb:Query",
                "dynamodb:UpdateItem"
            ],
            "Resource": [
                "${aws_dynamodb_table.releasy_templates.arn}",
                "${aws_dynamodb_table.releasy_releases.arn}"
            ]
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lambda_logs" {
  role = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

resource "aws_iam_role_policy_attachment" "lambda_invoke" {
  role = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaRole"
}

#CONFIGURE PERMISSIONS
resource "aws_lambda_permission" "apigw" {
  statement_id = "AllowAPIGatewayInvoke"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.releasy_auth_lambda.function_name
  principal = "apigateway.amazonaws.com"

  # The "/*/*" portion grants access from any method on any resource
  # within the API Gateway REST API.
  source_arn = "${aws_api_gateway_rest_api.example.execution_arn}/*/*"
}

# CONFIGURE API-GATEWAY RESOURCES
resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.example.id
  parent_id = aws_api_gateway_rest_api.example.root_resource_id
  path_part = "{proxy+}"
}

resource "aws_api_gateway_method" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.example.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "lambda" {
  rest_api_id = aws_api_gateway_rest_api.example.id
  resource_id = aws_api_gateway_method.proxy.resource_id
  http_method = aws_api_gateway_method.proxy.http_method

  integration_http_method = "POST"
  type = "AWS_PROXY"
  uri = aws_lambda_function.releasy_auth_lambda.invoke_arn
}

resource "aws_api_gateway_method" "proxy_root" {
  rest_api_id = aws_api_gateway_rest_api.example.id
  resource_id = aws_api_gateway_rest_api.example.root_resource_id
  http_method = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "lambda_root" {
  rest_api_id = aws_api_gateway_rest_api.example.id
  resource_id = aws_api_gateway_method.proxy_root.resource_id
  http_method = aws_api_gateway_method.proxy_root.http_method

  integration_http_method = "POST"
  type = "AWS_PROXY"
  uri = aws_lambda_function.releasy_auth_lambda.invoke_arn
}

# CONFIGURE DEPLOYMENT
resource "aws_api_gateway_deployment" "example" {
  depends_on = [
    aws_api_gateway_integration.lambda,
    aws_api_gateway_integration.lambda_root,
  ]

  rest_api_id = aws_api_gateway_rest_api.example.id
  stage_name = "prod"
}
