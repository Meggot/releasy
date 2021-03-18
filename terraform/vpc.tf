module "vpc" {

  source = "terraform-aws-modules/vpc/aws"
  version = "~> 2.24"


  name = var.vpc_name
  cidr = var.aws_vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support = true

  enable_nat_gateway = true
  single_nat_gateway = false

  # Skip creation of EIPs for the NAT Gateways
  reuse_nat_ips = true
  # IPs specified here as input to the module
  external_nat_ip_ids = aws_eip.nat.*.id

  azs = var.aws_availability_zones
  private_subnets = var.aws_private_subnets
  public_subnets = var.aws_public_subnets
  tags = {
    terraform = "true"
    environment = "dev"
  }
}

resource "aws_eip" "nat" {
  count = 2
  vpc = true
}

resource "aws_lb" "releasy_alb" {
  name = "releasy-alb"
  internal = true
  load_balancer_type = "application"
  subnets = module.vpc.private_subnets

  enable_deletion_protection = false
  security_groups = [
    module.vpc.default_security_group_id,
    aws_security_group.releasy_alb_sg.id]

  access_logs {
    bucket = aws_s3_bucket.releasy_releases.bucket
    prefix = "alb-logs"
    enabled = true
  }

  tags = {
    terraform = "true"
    environment = "dev"
  }
}

resource "aws_lb_target_group" "releasy_alb_tg" {
  name = "releasy-alb-tg-${substr(uuid(),0, 5)}"
  port = 80
  protocol = "HTTP"
  vpc_id = module.vpc.vpc_id
  target_type = "ip"
  stickiness {
    enabled = false
    type = "lb_cookie"
  }
  lifecycle {
    create_before_destroy = true
  }
  health_check {
    path = "/actuator/health"
    port = "6078"
    interval = 300
  }
}

resource "aws_lb_listener" "releasy_nlb_listener" {
  load_balancer_arn = aws_lb.releasy_alb.arn
  port = "80"
  protocol = "HTTP"

  default_action {
    type = "forward"
    target_group_arn = aws_lb_target_group.releasy_alb_tg.arn
  }
}

resource "aws_security_group" "releasy_alb_sg" {
  name = "releasy-alb-sg"
  description = "Releasy Load Balancer"
  vpc_id = module.vpc.vpc_id

  ingress {
    security_groups = [
      aws_security_group.releasy_lambda_sg.id]
    # TLS (change to whatever ports you need)
    from_port = 0
    to_port = 65535
    protocol = "tcp"

    cidr_blocks = [
      var.kings_place_cidr]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}
