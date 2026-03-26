package handler_test

import (
	"context"
	"encoding/json"
	"testing"

	"authorization/function/internal/handler"

	"github.com/aws/aws-lambda-go/events"
)

// TestHandler_HandleAPIGatewayV2 はハンドラが 200・JSON・本文フィールドを満たすことを検証する。
// RequestContext の最小フィールドだけ埋めた擬似イベントを使う（実 API の全項目は不要）。
func TestHandler_HandleAPIGatewayV2(t *testing.T) {
	t.Parallel()

	h := handler.New(nil)

	tests := []struct {
		name       string
		req        events.APIGatewayV2HTTPRequest
		wantPath   string
		wantMethod string
	}{
		{
			name: "GET root",
			req: events.APIGatewayV2HTTPRequest{
				RawPath: "/",
				RequestContext: events.APIGatewayV2HTTPRequestContext{
					RequestID: "req-root",
					HTTP: events.APIGatewayV2HTTPRequestContextHTTPDescription{
						Method: "GET",
					},
				},
			},
			wantPath:   "/",
			wantMethod: "GET",
		},
		{
			name: "POST nested path",
			req: events.APIGatewayV2HTTPRequest{
				RawPath: "/v1/clients",
				RequestContext: events.APIGatewayV2HTTPRequestContext{
					RequestID: "req-clients",
					HTTP: events.APIGatewayV2HTTPRequestContextHTTPDescription{
						Method: "POST",
					},
				},
			},
			wantPath:   "/v1/clients",
			wantMethod: "POST",
		},
		{
			// API Gateway によっては RawPath が空のケースがあり得るため、ハンドラが panic しないことも確認する。
			name: "empty raw path still echoed",
			req: events.APIGatewayV2HTTPRequest{
				RawPath: "",
				RequestContext: events.APIGatewayV2HTTPRequestContext{
					RequestID: "req-empty-path",
					HTTP: events.APIGatewayV2HTTPRequestContextHTTPDescription{
						Method: "OPTIONS",
					},
				},
			},
			wantPath:   "",
			wantMethod: "OPTIONS",
		},
	}

	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			resp, err := h.HandleAPIGatewayV2(context.Background(), tt.req)
			if err != nil {
				t.Fatalf("HandleAPIGatewayV2: %v", err)
			}
			if resp.StatusCode != 200 {
				t.Fatalf("StatusCode = %d, want 200", resp.StatusCode)
			}
			if got := resp.Headers["Content-Type"]; got != "application/json" {
				t.Fatalf("Content-Type = %q, want application/json", got)
			}

			var payload struct {
				OK     bool   `json:"ok"`
				Path   string `json:"path"`
				Method string `json:"method"`
			}
			if err := json.Unmarshal([]byte(resp.Body), &payload); err != nil {
				t.Fatalf("body JSON: %v\nbody=%q", err, resp.Body)
			}
			if !payload.OK {
				t.Fatal(`want ok: true`)
			}
			if payload.Path != tt.wantPath {
				t.Fatalf("path = %q, want %q", payload.Path, tt.wantPath)
			}
			if payload.Method != tt.wantMethod {
				t.Fatalf("method = %q, want %q", payload.Method, tt.wantMethod)
			}
		})
	}
}

// BenchmarkHandler_HandleAPIGatewayV2 は JSON 応答生成コストの目安を計測する（最適化前後の比較など）。
func BenchmarkHandler_HandleAPIGatewayV2(b *testing.B) {
	h := handler.New(nil)
	req := events.APIGatewayV2HTTPRequest{
		RawPath: "/bench",
		RequestContext: events.APIGatewayV2HTTPRequestContext{
			RequestID: "bench",
			HTTP: events.APIGatewayV2HTTPRequestContextHTTPDescription{
				Method: "GET",
			},
		},
	}
	ctx := context.Background()

	b.ReportAllocs()
	for i := 0; i < b.N; i++ {
		_, err := h.HandleAPIGatewayV2(ctx, req)
		if err != nil {
			b.Fatal(err)
		}
	}
}
