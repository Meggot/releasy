terraform {
  backend "s3" {
    bucket = "meggot-slackbots"
    region = "eu-west-1"
    key = "releasy/releasy.tfstate"
  }
}
