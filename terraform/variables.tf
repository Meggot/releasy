variable "target_dir" {
  description = "Lambda Build Location"
}

variable "slack_signing_secret" {
  description = "Slack Bot Signing Secret"
  default = ""
}

variable "slack_token" {
  description = "Slack Bot Token"
  default = ""
}

variable "app_name" {
  description = "App Name"
  default = "releasy"
}

variable "app_version" {
  description = "App version"
  default = "1"
}

variable "vpc_name" {
  description = "VPC Name"
  default = "Releasy"
}

variable "aws_region" {
  description = "AWS Region"
  default = "eu-west-1"
}

variable "aws_availability_zones" {
  description = "AWS availability zones"
  type = list
  default = [
    "eu-west-1a",
    "eu-west-1b"]
}

variable "aws_vpc_cidr" {
  description = "CIDR for this vpc"
  default = "172.16.6.0/24"
}

variable "kings_place_cidr" {
  description = "Kings Place office IP"
  default = "217.138.28.22/32"
}

variable "aws_private_subnets" {
  description = "Private subnets for region"
  type = list
  default = [
    "172.16.6.0/26",
    "172.16.6.64/26"]
}

variable "aws_public_subnets" {
  description = "Public subnets for region"
  type = list
  default = [
    "172.16.6.128/26",
    "172.16.6.192/26"]
}

variable "confluence_api_token" {
  description = "The confluence token which allow API requests to confluence. Releasy User."
  default = ""
}

variable "jenkins_username" {
  description = "Username login details to talk to Jenkins."
  default = ""
}

variable "jenkins_password" {
  description = "Password login details to talk to Jenkins."
  default = ""
}
