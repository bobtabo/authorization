// Package handler は API Gateway / Lambda のイベントとドメイン層の橋渡しを行う。
package handler

import (
	"context"
	"io"
	"log/slog"
	"net/http"
	"net/url"
	"os"
	"strings"

	"authorization/function/internal/adapter"

	"github.com/aws/aws-lambda-go/events"
)

// albURL は ALB（ローカルでは nginx-proxy）のベース URL。
// 環境変数 ALB_URL で上書き可能。
var albURL = getenv("ALB_URL", "https://auth-proxy")

// hostMap はパスプレフィックスから nginx-proxy が振り分けに使う Host ヘッダー値を返す。
var hostMap = map[string]string{
	"/function/php":       "apis.authorization-php.dev",
	"/function/go-gin":    "apis.authorization-go-gin.dev",
	"/function/go-beego":  "apis.authorization-go-beego.dev",
	"/function/go-echo":   "apis.authorization-go-echo.dev",
	"/function/kotlin":    "apis.authorization-kotlin.dev",
	"/function/python":    "apis.authorization-python.dev",
	"/function/rb-hanami": "apis.authorization-rb-hanami.dev",
	"/function/rb-rails":  "apis.authorization-rb-rails.dev",
	"/function/rust":      "apis.authorization-rust.dev",
	"/function/ts":        "apis.authorization-ts.dev",
}

func getenv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

// Handler は REST API (v1) 用の Lambda ハンドラ。外部依存は adapter 経由で注入する。
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

// Handle は REST API (v1) のリクエストを処理する。
// パスプレフィックス（/function/php 等）で対象バックエンドを判定し、リクエストを転送する。
func (h *Handler) Handle(
	ctx context.Context,
	req events.APIGatewayProxyRequest,
) (events.APIGatewayProxyResponse, error) {
	slog.InfoContext(ctx, "request",
		"method", req.HTTPMethod,
		"path", req.Path,
		"request_id", req.RequestContext.RequestID,
	)

	// パスプレフィックスからバックエンド側パスと Host ヘッダーを決定する
	host, backendPath := resolveBackend(req.Path)
	if host == "" {
		return events.APIGatewayProxyResponse{
			StatusCode: 404,
			Headers:    map[string]string{},
		}, nil
	}

	targetURL := albURL + backendPath
	if len(req.MultiValueQueryStringParameters) > 0 {
		vals := url.Values{}
		for k, vs := range req.MultiValueQueryStringParameters {
			for _, v := range vs {
				vals.Add(k, v)
			}
		}
		targetURL += "?" + vals.Encode()
	}

	// HTTP リクエストを組み立てる
	httpReq, err := newRequest(ctx, req.HTTPMethod, targetURL, req.Body, req.Headers)
	if err != nil {
		slog.ErrorContext(ctx, "build request", "error", err)
		return events.APIGatewayProxyResponse{
			StatusCode: 500,
			Headers:    map[string]string{},
		}, err
	}
	httpReq.Host = host

	// バックエンドへ転送する
	resp, err := h.deps.HTTPClient().Do(httpReq)
	if err != nil {
		slog.ErrorContext(ctx, "forward request", "url", targetURL, "error", err)
		return events.APIGatewayProxyResponse{
			StatusCode: 502,
			Headers:    map[string]string{},
		}, err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return events.APIGatewayProxyResponse{
			StatusCode: 500,
			Headers:    map[string]string{},
		}, err
	}

	// Set-Cookie は複数値を持てるため MultiValueHeaders へ、それ以外は Headers へ
	headers := make(map[string]string, len(resp.Header))
	multiValueHeaders := make(map[string][]string)
	for k, v := range resp.Header {
		if strings.EqualFold(k, "set-cookie") {
			multiValueHeaders[k] = v
		} else {
			headers[k] = v[0]
		}
	}

	return events.APIGatewayProxyResponse{
		StatusCode:        resp.StatusCode,
		Headers:           headers,
		MultiValueHeaders: multiValueHeaders,
		Body:              string(body),
	}, nil
}

// resolveBackend はパスを見て Host ヘッダー値と転送先パスを返す。
// 一致するプレフィックスがなければ空文字列を返す。
// /function/go が /function/go-beego に誤マッチしないよう、
// プレフィックスの直後が "/" または末尾であることを確認する。
func resolveBackend(rawPath string) (host, path string) {
	for prefix, h := range hostMap {
		if rawPath == prefix || strings.HasPrefix(rawPath, prefix+"/") {
			p := strings.TrimPrefix(rawPath, prefix)
			if p == "" {
				p = "/"
			}
			return h, p
		}
	}
	return "", ""
}

// newRequest は API Gateway イベントのフィールドから *http.Request を組み立てる。
func newRequest(ctx context.Context, method, targetURL, body string, headers map[string]string) (*http.Request, error) {
	var bodyReader io.Reader
	if body != "" {
		bodyReader = strings.NewReader(body)
	}

	req, err := http.NewRequestWithContext(ctx, method, targetURL, bodyReader)
	if err != nil {
		return nil, err
	}
	for k, v := range headers {
		req.Header.Set(k, v)
	}
	return req, nil
}
