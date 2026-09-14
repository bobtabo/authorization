// Package main はローカル開発用の HTTP サーバーエントリ。
// API Gateway REST API（v1 プロキシ統合）が行う HTTP ↔ Lambda イベント変換を担い、
// Lambda コンテナ（RIE）へリクエストを転送する。
// 本番では API Gateway が同等の変換を行うため、ローカルのみで使用する。
package main

import (
	"bytes"
	"context"
	"encoding/json"
	"io"
	"log/slog"
	"net/http"
	"os"
	"sync"

	"github.com/aws/aws-lambda-go/events"
)

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
	lambdaInvokeURL := os.Getenv("LAMBDA_INVOKE_URL")
	if lambdaInvokeURL == "" {
		lambdaInvokeURL = "http://localhost:9000/2015-03-31/functions/function/invocations"
	}

	invoker := &lambdaInvoker{url: lambdaInvokeURL}

	// Lambda RIE はシングルスレッドで同時リクエストを処理できないため、
	// ミューテックスでシリアライズする（本番は API Gateway が並列 Lambda を起動する）。
	var mu sync.Mutex

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		mu.Lock()
		defer mu.Unlock()

		body, _ := io.ReadAll(r.Body)

		// HTTP リクエスト → APIGatewayProxyRequest に変換する（API Gateway 相当）
		headers := make(map[string]string, len(r.Header))
		multiHeaders := make(map[string][]string, len(r.Header))
		for k, v := range r.Header {
			headers[k] = v[0]
			multiHeaders[k] = v
		}
		query := r.URL.Query()
		queryParams := make(map[string]string, len(query))
		multiQueryParams := make(map[string][]string, len(query))
		for k, v := range query {
			queryParams[k] = v[0]
			multiQueryParams[k] = v
		}
		event := events.APIGatewayProxyRequest{
			Resource:                        "/{proxy+}",
			Path:                            r.URL.Path,
			HTTPMethod:                      r.Method,
			Headers:                         headers,
			MultiValueHeaders:               multiHeaders,
			QueryStringParameters:           queryParams,
			MultiValueQueryStringParameters: multiQueryParams,
			Body:                            string(body),
			RequestContext: events.APIGatewayProxyRequestContext{
				RequestID:  r.Header.Get("X-Request-Id"),
				HTTPMethod: r.Method,
				Path:       r.URL.Path,
			},
		}

		// Lambda コンテナ（RIE）を呼び出す
		resp, err := invoker.invoke(r.Context(), event)
		if err != nil {
			slog.Error("lambda invoke error", "error", err)
			http.Error(w, "bad gateway", http.StatusBadGateway)
			return
		}

		// APIGatewayProxyResponse → HTTP レスポンスに変換する（API Gateway 相当）
		for k, v := range resp.Headers {
			w.Header().Set(k, v)
		}
		for k, vs := range resp.MultiValueHeaders {
			w.Header().Del(k)
			for _, v := range vs {
				w.Header().Add(k, v)
			}
		}
		w.WriteHeader(resp.StatusCode)
		_, _ = w.Write([]byte(resp.Body))
	})

	slog.Info("apigw-emulator started", "port", port, "lambda", lambdaInvokeURL)
	if err := http.ListenAndServe(":"+port, nil); err != nil {
		slog.Error("server error", "error", err)
		os.Exit(1)
	}
}

type lambdaInvoker struct {
	url string
}

// invoke は Lambda RIE のエンドポイントへイベントを送信し、レスポンスを返す。
func (l *lambdaInvoker) invoke(ctx context.Context, event events.APIGatewayProxyRequest) (*events.APIGatewayProxyResponse, error) {
	payload, err := json.Marshal(event)
	if err != nil {
		return nil, err
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, l.url, bytes.NewReader(payload))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")

	httpResp, err := http.DefaultClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer httpResp.Body.Close()

	var lambdaResp events.APIGatewayProxyResponse
	if err := json.NewDecoder(httpResp.Body).Decode(&lambdaResp); err != nil {
		return nil, err
	}
	return &lambdaResp, nil
}
