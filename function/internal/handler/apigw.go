// Package handler は API Gateway / Lambda のイベントとドメイン層の橋渡しを行う。
package handler

import (
	"context"
	"encoding/json"
	"log/slog"

	"authorization/function/internal/adapter"
	"authorization/function/internal/domain"

	"github.com/aws/aws-lambda-go/events"
)

// Handler は HTTP API v2 用の Lambda ハンドラ。外部依存は adapter 経由で注入する。
type Handler struct {
	deps *adapter.Deps
}

// New は Handler を組み立てる。deps が nil のときは adapter.NewDeps() を使う。
func New(deps *adapter.Deps) *Handler {
	if deps == nil {
		deps = adapter.NewDeps()
	}
	return &Handler{deps: deps}
}

// HandleAPIGatewayV2 は HTTP API v2 のリクエストを処理する。
// 認可やルーティングの本体はここに追加する想定である。
// 現状は疎通用に method と path を JSON で返すのみ。
func (h *Handler) HandleAPIGatewayV2(
	ctx context.Context,
	req events.APIGatewayV2HTTPRequest,
) (events.APIGatewayV2HTTPResponse, error) {
	_ = h.deps // 未使用だが DI 差し込み口として保持

	slog.InfoContext(ctx, "request",
		"method", req.RequestContext.HTTP.Method,
		"path", req.RawPath,
		"request_id", req.RequestContext.RequestID,
	)

	body, err := json.Marshal(domain.PingResponse{
		OK:     true,
		Path:   req.RawPath,
		Method: req.RequestContext.HTTP.Method,
	})
	if err != nil {
		// domain.PingResponse の Marshal は通常失敗しないが、型を変えたときのために分岐を残す。
		return events.APIGatewayV2HTTPResponse{StatusCode: 500}, err
	}

	return events.APIGatewayV2HTTPResponse{
		StatusCode: 200,
		Headers: map[string]string{
			"Content-Type": "application/json",
		},
		Body: string(body),
	}, nil
}
