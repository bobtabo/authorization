output "api_gateway_id" {
  description = "API Gateway REST API ID"
  value       = aws_api_gateway_rest_api.main.id
}

output "api_gateway_url" {
  description = "API Gateway のローカルエンドポイント URL（ホストからアクセス）"
  value       = "http://localhost:4566/restapis/${aws_api_gateway_rest_api.main.id}/${var.stage_name}/_user_request_"
}

output "api_gateway_docker_url" {
  description = "API Gateway の Docker 内エンドポイント URL（コンテナ間通信用）"
  value       = "http://localstack:4566/restapis/${aws_api_gateway_rest_api.main.id}/${var.stage_name}/_user_request_"
}

output "lambda_function_name" {
  description = "Lambda 関数名"
  value       = aws_lambda_function.http_function.function_name
}

output "ses_domain" {
  description = "SES 検証済みドメイン"
  value       = aws_ses_domain_identity.main.domain
}

output "ses_from_address" {
  description = "SES 送信元メールアドレス"
  value       = aws_ses_email_identity.sender.email
}

output "ssm_parameter_prefix" {
  description = "SSM Parameter Store のプレフィックス"
  value       = "/authorization"
}

output "ssm_parameter_paths" {
  description = "作成された SSM パラメータのパス一覧"
  value = [
    aws_ssm_parameter.db_host.name,
    aws_ssm_parameter.db_port.name,
    aws_ssm_parameter.db_name.name,
    aws_ssm_parameter.db_username.name,
    aws_ssm_parameter.db_password.name,
    aws_ssm_parameter.redis_host.name,
    aws_ssm_parameter.redis_port.name,
    aws_ssm_parameter.google_client_id.name,
    aws_ssm_parameter.google_client_secret.name,
    aws_ssm_parameter.github_client_id.name,
    aws_ssm_parameter.github_client_secret.name,
    aws_ssm_parameter.app_env.name,
    aws_ssm_parameter.app_jwt_secret.name,
  ]
}
