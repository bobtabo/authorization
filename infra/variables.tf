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

# ──────────────────────────────────────────────
# SSM Parameter Store 変数
# ──────────────────────────────────────────────

variable "ssm_db_host" {
  description = "SSM: データベースホスト"
  type        = string
  default     = "host.docker.internal"
}

variable "ssm_db_port" {
  description = "SSM: データベースポート"
  type        = string
  default     = "3306"
}

variable "ssm_db_name" {
  description = "SSM: データベース名"
  type        = string
  default     = "authorization"
}

variable "ssm_db_username" {
  description = "SSM: データベースユーザー名"
  type        = string
  default     = "your-db-username"
  sensitive   = true
}

variable "ssm_db_password" {
  description = "SSM: データベースパスワード"
  type        = string
  default     = "your-db-password"
  sensitive   = true
}

variable "ssm_redis_host" {
  description = "SSM: Redis ホスト"
  type        = string
  default     = "host.docker.internal"
}

variable "ssm_redis_port" {
  description = "SSM: Redis ポート"
  type        = string
  default     = "6379"
}

variable "ssm_google_client_id" {
  description = "SSM: Google OAuth クライアント ID"
  type        = string
  default     = "your-google-client-id"
  sensitive   = true
}

variable "ssm_google_client_secret" {
  description = "SSM: Google OAuth クライアントシークレット"
  type        = string
  default     = "your-google-client-secret"
  sensitive   = true
}

variable "ssm_github_client_id" {
  description = "SSM: GitHub OAuth クライアント ID"
  type        = string
  default     = "your-github-client-id"
  sensitive   = true
}

variable "ssm_github_client_secret" {
  description = "SSM: GitHub OAuth クライアントシークレット"
  type        = string
  default     = "your-github-client-secret"
  sensitive   = true
}

variable "ssm_app_env" {
  description = "SSM: アプリケーション環境名"
  type        = string
  default     = "local"
}

variable "ssm_app_jwt_secret" {
  description = "SSM: JWT シークレット"
  type        = string
  default     = "local-jwt-secret-key"
  sensitive   = true
}
