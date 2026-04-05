package handler_test

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"

	"authorization/function/internal/adapter"
	"authorization/function/internal/handler"

	"github.com/aws/aws-lambda-go/events"
)

// newTestDeps はテスト用の Deps を組み立てる。
// transport を差し替えることでバックエンド URL をモックサーバーへ向ける。
func newTestDeps(transport http.RoundTripper) *adapter.Deps {
	return adapter.NewDepsWithClient(&http.Client{Transport: transport})
}

// backendRedirect はリクエストを mock サーバーへリダイレクトする RoundTripper。
// テスト内でバックエンドのドメインを気にせず、mock に転送できる。
type backendRedirect struct {
	target string
}

func (b *backendRedirect) RoundTrip(req *http.Request) (*http.Response, error) {
	req2 := req.Clone(req.Context())
	req2.URL.Host = b.target
	req2.URL.Scheme = "http"
	return http.DefaultTransport.RoundTrip(req2)
}

func TestHandler_Proxy(t *testing.T) {
	t.Parallel()

	// モックバックエンドサーバー
	// t.Cleanup を使い、全サブテスト完了後に閉じる（defer だとサブテスト並列実行中に閉じてしまう）
	mock := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("X-Backend-Path", r.URL.Path)
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"ok":true}`))
	}))
	t.Cleanup(mock.Close)

	h := handler.New(newTestDeps(&backendRedirect{target: mock.Listener.Addr().String()}))

	tests := []struct {
		name           string
		rawPath        string
		method         string
		wantStatus     int
		wantBackend    string // X-Backend-Path ヘッダーで確認
	}{
		{
			name:        "PHP バックエンドへ転送",
			rawPath:     "/function/php/api/clients",
			method:      "GET",
			wantStatus:  200,
			wantBackend: "/api/clients",
		},
		{
			name:        "Go バックエンドへ転送",
			rawPath:     "/function/go/api/health",
			method:      "GET",
			wantStatus:  200,
			wantBackend: "/api/health",
		},
		{
			name:        "プレフィックスのみのパス",
			rawPath:     "/function/php",
			method:      "GET",
			wantStatus:  200,
			wantBackend: "/",
		},
	}

	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			resp, err := h.HandleAPIGatewayV2(context.Background(), events.APIGatewayV2HTTPRequest{
				RawPath: tt.rawPath,
				RequestContext: events.APIGatewayV2HTTPRequestContext{
					HTTP: events.APIGatewayV2HTTPRequestContextHTTPDescription{
						Method: tt.method,
					},
				},
			})
			if err != nil {
				t.Fatalf("HandleAPIGatewayV2: %v", err)
			}
			if resp.StatusCode != tt.wantStatus {
				t.Fatalf("StatusCode = %d, want %d", resp.StatusCode, tt.wantStatus)
			}
			if got := resp.Headers["X-Backend-Path"]; got != tt.wantBackend {
				t.Fatalf("X-Backend-Path = %q, want %q", got, tt.wantBackend)
			}
		})
	}
}

func TestHandler_UnknownPrefix_Returns404(t *testing.T) {
	t.Parallel()

	h := handler.New(nil)

	resp, err := h.HandleAPIGatewayV2(context.Background(), events.APIGatewayV2HTTPRequest{
		RawPath: "/unknown/path",
		RequestContext: events.APIGatewayV2HTTPRequestContext{
			HTTP: events.APIGatewayV2HTTPRequestContextHTTPDescription{Method: "GET"},
		},
	})
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if resp.StatusCode != 404 {
		t.Fatalf("StatusCode = %d, want 404", resp.StatusCode)
	}
}
