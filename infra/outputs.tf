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
