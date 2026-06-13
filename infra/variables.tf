variable "aws_region" {
  description = "AWS リージョン（LocalStack はリージョンを問わない）"
  type        = string
  default     = "ap-northeast-1"
}

variable "localstack_endpoint" {
  description = "LocalStack のエンドポイント URL"
  type        = string
  default     = "http://localhost:4566"
}

variable "lambda_zip_path" {
  description = "Lambda デプロイ用 ZIP ファイルのパス"
  type        = string
  default     = "../function/function.zip"
}

variable "stage_name" {
  description = "API Gateway のステージ名"
  type        = string
  default     = "local"
}

variable "ses_domain" {
  description = "SES で検証するドメイン"
  type        = string
  default     = "example.com"
}

variable "ses_from_address" {
  description = "SES 送信元メールアドレス"
  type        = string
  default     = "no-reply@example.com"
}
