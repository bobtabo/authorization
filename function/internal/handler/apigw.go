// Package handler は API Gateway / Lambda のイベントとドメイン層の橋渡しを行う。
package handler

import (
	"context"
	"io"
	"log/slog"
	"net/http"
	"os"
	"strings"

	"authorization/function/internal/adapter"

	"github.com/aws/aws-lambda-go/events"
)

// backendURL はパスプレフィックスからバックエンドのベース URL を返す。
// 環境変数で上書き可能（本番デプロイ時など）。
var backendURL = map[string]string{
	"/function/php":    getenv("BACKEND_PHP_URL", "https://apis.authorization-php.dev"),
	"/function/go":     getenv("BACKEND_GO_URL", "https://apis.authorization-go.dev"),
	"/function/python": getenv("BACKEND_PYTHON_URL", "https://apis.authorization-python.dev"),
	"/function/ts":     getenv("BACKEND_TS_URL", "https://apis.authorization-ts.dev"),
}

func getenv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

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
// パスプレフィックス（/function/php 等）で対象バックエンドを判定し、リクエストを転送する。
func (h *Handler) HandleAPIGatewayV2(
	ctx context.Context,
	req events.APIGatewayV2HTTPRequest,
) (events.APIGatewayV2HTTPResponse, error) {
	slog.InfoContext(ctx, "request",
		"method", req.RequestContext.HTTP.Method,
		"path", req.RawPath,
		"request_id", req.RequestContext.RequestID,
	)

	// パスプレフィックスからバックエンド URL とバックエンド側パスを決定する
	base, backendPath := resolveBackend(req.RawPath)
	if base == "" {
		return events.APIGatewayV2HTTPResponse{StatusCode: 404}, nil
	}

	targetURL := base + backendPath
	if req.RawQueryString != "" {
		targetURL += "?" + req.RawQueryString
	}

	// HTTP リクエストを組み立てる
	httpReq, err := newRequest(ctx, req.RequestContext.HTTP.Method, targetURL, req.Body, req.Headers)
	if err != nil {
		slog.ErrorContext(ctx, "build request", "error", err)
		return events.APIGatewayV2HTTPResponse{StatusCode: 500}, err
	}

	// バックエンドへ転送する
	resp, err := h.deps.HTTPClient().Do(httpReq)
	if err != nil {
		slog.ErrorContext(ctx, "forward request", "url", targetURL, "error", err)
		return events.APIGatewayV2HTTPResponse{StatusCode: 502}, err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return events.APIGatewayV2HTTPResponse{StatusCode: 500}, err
	}

	// Set-Cookie は複数値を持てるため Cookies フィールドへ、それ以外は Headers へ
	headers := make(map[string]string, len(resp.Header))
	var cookies []string
	for k, v := range resp.Header {
		if strings.EqualFold(k, "set-cookie") {
			cookies = append(cookies, v...)
		} else {
			headers[k] = v[0]
		}
	}

	return events.APIGatewayV2HTTPResponse{
		StatusCode: resp.StatusCode,
		Headers:    headers,
		Cookies:    cookies,
		Body:       string(body),
	}, nil
}

// resolveBackend はパスを見てバックエンドのベース URL と転送先パスを返す。
// 一致するプレフィックスがなければ空文字列を返す。
func resolveBackend(rawPath string) (base, path string) {
	for prefix, url := range backendURL {
		if strings.HasPrefix(rawPath, prefix) {
			p := strings.TrimPrefix(rawPath, prefix)
			if p == "" {
				p = "/"
			}
			return url, p
		}
	}
	return "", ""
}

// newRequest は API Gateway イベントのフィールドから *http.Request を組み立てる。
func newRequest(ctx context.Context, method, url, body string, headers map[string]string) (*http.Request, error) {
	var bodyReader io.Reader
	if body != "" {
		bodyReader = strings.NewReader(body)
	}

	req, err := http.NewRequestWithContext(ctx, method, url, bodyReader)
	if err != nil {
		return nil, err
	}
	for k, v := range headers {
		req.Header.Set(k, v)
	}
	return req, nil
}
