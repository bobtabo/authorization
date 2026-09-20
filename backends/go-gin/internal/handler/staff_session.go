package handler

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"strconv"
	"strings"
)

// SignStaffID は staff_id クッキーの値を "{staffID}.{HMAC-SHA256署名}" 形式で
// 署名します。secretを知らない第三者は staffID を改ざんしても正しい署名を
// 作成できないため、クッキー値の改ざん（なりすまし）を防げます。
// テストコードから正規のクッキー値を組み立てるために公開しています。
func SignStaffID(staffID uint, secret string) string {
	payload := strconv.FormatUint(uint64(staffID), 10)
	return payload + "." + hmacHex(payload, secret)
}

// verifyStaffID は署名済み staff_id クッキーの値を検証し、staffID を返します。
// 署名が不正・形式不正の場合は 0（未認証）を返します。
func verifyStaffID(value string, secret string) uint {
	payload, sig, ok := strings.Cut(value, ".")
	if !ok || payload == "" || sig == "" {
		return 0
	}
	if !hmac.Equal([]byte(sig), []byte(hmacHex(payload, secret))) {
		return 0
	}
	id, err := strconv.ParseUint(payload, 10, 32)
	if err != nil {
		return 0
	}
	return uint(id)
}

// hmacHex は HMAC-SHA256(payload, secret) の16進数文字列を返します。
func hmacHex(payload string, secret string) string {
	mac := hmac.New(sha256.New, []byte(secret))
	mac.Write([]byte(payload))
	return hex.EncodeToString(mac.Sum(nil))
}
