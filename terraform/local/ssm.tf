# ──────────────────────────────────────────────
# SSM Parameter Store リソース定義
# LocalStack SSM でシークレット管理をエミュレートする
# ──────────────────────────────────────────────

# データベース接続情報
resource "aws_ssm_parameter" "db_host" {
  name  = "/authorization/database/host"
  type  = "String"
  value = var.ssm_db_host
}

resource "aws_ssm_parameter" "db_port" {
  name  = "/authorization/database/port"
  type  = "String"
  value = var.ssm_db_port
}

resource "aws_ssm_parameter" "db_name" {
  name  = "/authorization/database/name"
  type  = "String"
  value = var.ssm_db_name
}

resource "aws_ssm_parameter" "db_username" {
  name  = "/authorization/database/username"
  type  = "SecureString"
  value = var.ssm_db_username
}

resource "aws_ssm_parameter" "db_password" {
  name  = "/authorization/database/password"
  type  = "SecureString"
  value = var.ssm_db_password
}

# Redis 接続情報
resource "aws_ssm_parameter" "redis_host" {
  name  = "/authorization/redis/host"
  type  = "String"
  value = var.ssm_redis_host
}

resource "aws_ssm_parameter" "redis_port" {
  name  = "/authorization/redis/port"
  type  = "String"
  value = var.ssm_redis_port
}

# OAuth2 クライアント情報（Google）
resource "aws_ssm_parameter" "google_client_id" {
  name  = "/authorization/oauth/google/client_id"
  type  = "SecureString"
  value = var.ssm_google_client_id
}

resource "aws_ssm_parameter" "google_client_secret" {
  name  = "/authorization/oauth/google/client_secret"
  type  = "SecureString"
  value = var.ssm_google_client_secret
}

# OAuth2 クライアント情報（GitHub）
resource "aws_ssm_parameter" "github_client_id" {
  name  = "/authorization/oauth/github/client_id"
  type  = "SecureString"
  value = var.ssm_github_client_id
}

resource "aws_ssm_parameter" "github_client_secret" {
  name  = "/authorization/oauth/github/client_secret"
  type  = "SecureString"
  value = var.ssm_github_client_secret
}

# アプリケーション共通設定
resource "aws_ssm_parameter" "app_env" {
  name  = "/authorization/app/env"
  type  = "String"
  value = var.ssm_app_env
}

resource "aws_ssm_parameter" "app_jwt_secret" {
  name  = "/authorization/app/jwt_secret"
  type  = "SecureString"
  value = var.ssm_app_jwt_secret
}
