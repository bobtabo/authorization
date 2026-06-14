# ──────────────────────────────────────────────
# SES リソース定義
# LocalStack SES でメール送信をエミュレートする
# ──────────────────────────────────────────────

# ドメイン検証（LocalStack では自動的に検証済みになる）
resource "aws_ses_domain_identity" "main" {
  domain = var.ses_domain
}

# 送信元メールアドレスの検証
resource "aws_ses_email_identity" "sender" {
  email = var.ses_from_address
}
