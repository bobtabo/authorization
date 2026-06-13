# ──────────────────────────────────────────────
# LocalStack 向け Terraform 定義
# tflocal apply で API Gateway + Lambda + SES + SSM を構築する
# ──────────────────────────────────────────────

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region                      = var.aws_region
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    apigateway = var.localstack_endpoint
    iam        = var.localstack_endpoint
    lambda     = var.localstack_endpoint
    s3         = var.localstack_endpoint
    ses        = var.localstack_endpoint
    ssm        = var.localstack_endpoint
  }
}
