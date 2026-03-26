// Package main は AWS Lambda（custom runtime provided.al2023）のエントリ。
// API Gateway HTTP API（v2）または Lambda Function URL から渡るペイロードを想定している。
package main

import (
	"log/slog"
	"os"

	"authorization/function/internal/adapter"
	"authorization/function/internal/handler"

	"github.com/aws/aws-lambda-go/lambda"
)

// init はデフォルトの slog を JSON かつ stderr 出力に設定する。
// CloudWatch Logs は stderr を拾うため、構造化ログは stderr に出す。
func init() {
	h := slog.NewJSONHandler(os.Stderr, &slog.HandlerOptions{Level: slog.LevelInfo})
	slog.SetDefault(slog.New(h))
}

// main は依存を組み立て、Lambda ランタイムにハンドラを登録する。
func main() {
	deps := adapter.NewDeps()
	h := handler.New(deps)
	lambda.Start(h.HandleAPIGatewayV2)
}
