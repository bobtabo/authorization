package handler

import (
	"crypto/rand"
	"crypto/subtle"
	"encoding/hex"
	"strings"
)

// oauthStateCookieName は OAuth 認可開始時に発行する nonce を保持するクッキー名です。
const oauthStateCookieName = "oauth_state"

// oauthStateCookieMaxAge は nonce クッキーの有効期間（秒）です。
const oauthStateCookieMaxAge = 600

// newOAuthNonce は CSRF 対策用のランダムな nonce（16バイトの16進表現）を生成します。
func newOAuthNonce() (string, error) {
	b := make([]byte, 16)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	return hex.EncodeToString(b), nil
}

// buildOAuthState は state パラメータを組み立てます。
// 形式: "{runtime}|{nonce}" または "{runtime}|{nonce}|{invitationToken}"
func buildOAuthState(runtime, nonce, invitationToken string) string {
	if invitationToken == "" {
		return runtime + "|" + nonce
	}
	return runtime + "|" + nonce + "|" + invitationToken
}

// parseOAuthState は state パラメータを nonce と招待トークンに分解します。
// nonce セグメントが無い場合は ok=false を返します。
func parseOAuthState(state string) (nonce, invitationToken string, ok bool) {
	parts := strings.SplitN(state, "|", 3)
	if len(parts) < 2 || parts[1] == "" {
		return "", "", false
	}
	nonce = parts[1]
	if len(parts) == 3 {
		invitationToken = parts[2]
	}
	return nonce, invitationToken, true
}

// verifyOAuthNonce は state 中の nonce とクッキーに保存した nonce を定数時間で比較します。
func verifyOAuthNonce(expected, actual string) bool {
	if expected == "" || actual == "" {
		return false
	}
	return subtle.ConstantTimeCompare([]byte(expected), []byte(actual)) == 1
}
