resource "aws_secretsmanager_secret" "confluence_api_token" {
  name = "confluence_api_token.3"

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_secretsmanager_secret" "jenkins_username" {
  name = "jenkins_username.3"

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_secretsmanager_secret" "jenkins_password" {
  name = "jenkins_password.3"

  lifecycle {
    prevent_destroy = true
  }
}


resource "aws_secretsmanager_secret_version" "confluence_api_token_version" {
  secret_id = "${aws_secretsmanager_secret.confluence_api_token.id}"
  secret_string = "${var.confluence_api_token}"
}

resource "aws_secretsmanager_secret_version" "jenkins_username_version" {
  secret_id = "${aws_secretsmanager_secret.jenkins_username.id}"
  secret_string = "${var.jenkins_username}"
}

resource "aws_secretsmanager_secret_version" "jenkins_password_version" {
  secret_id = "${aws_secretsmanager_secret.jenkins_password.id}"
  secret_string = "${var.jenkins_username}"
}


resource "aws_iam_role_policy" "releasy_secrets_access" {
  name = "releasy_secrets_access"
  role = "${aws_iam_role.ecsTaskExecutionRole.id}"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
        "Effect": "Allow",
        "Action": [
          "kms:GetSecretValue",
          "kms:CreateSecret"
        ],
        "Resource": [
          "${aws_secretsmanager_secret.confluence_api_token.arn}",
          "${aws_secretsmanager_secret.jenkins_username.arn}",
          "${aws_secretsmanager_secret.jenkins_password.arn}"
        ]
    }
  ]
}
EOF
}