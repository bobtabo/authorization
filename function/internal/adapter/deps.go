// Package adapter は永続化（DynamoDB 等）や外部 HTTP API などの境界実装を置く。
package adapter

import (
	"crypto/tls"
	"net/http"
	"os"
)

// Deps はハンドラが依存する外部リソースを束ねる（DI 用）。
type Deps struct {
	httpClient *http.Client
}

// NewDeps は依存のデフォルト構成を返す。
// INSECURE_SKIP_TLS_VERIFY=true のとき TLS 検証をスキップする（ローカル自己署名証明書用）。
// リダイレクトは追従せずそのまま返す（OAuth の 302 をブラウザに渡すため）。
func NewDeps() *Deps {
	transport := http.DefaultTransport
	if os.Getenv("INSECURE_SKIP_TLS_VERIFY") == "true" {
		transport = &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true}, //nolint:gosec
		}
	}
	return &Deps{
		httpClient: &http.Client{
			Transport: transport,
			CheckRedirect: func(_ *http.Request, _ []*http.Request) error {
				return http.ErrUseLastResponse
			},
		},
	}
}

// NewDepsWithClient は HTTP クライアントを指定して Deps を組み立てる（テスト用）。
func NewDepsWithClient(client *http.Client) *Deps {
	return &Deps{httpClient: client}
}

// HTTPClient は外部リクエスト用の HTTP クライアントを返す。
func (d *Deps) HTTPClient() *http.Client {
	return d.httpClient
}
