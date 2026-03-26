// Package domain はこの Lambda 専用のドメイン型・ビジネスルールを置く（認可ロジック等はここへ）。
package domain

// PingResponse は疎通用 API の JSON 本文（後からフィールドを増やす）。
type PingResponse struct {
	OK     bool   `json:"ok"`
	Path   string `json:"path"`
	Method string `json:"method"`
}
