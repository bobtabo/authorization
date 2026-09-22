package handler

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"strconv"
	"strings"
	"time"
)

// SignStaffID は staff_id クッキーの値を "{staffID}.{有効期限のUnix秒}.{HMAC-SHA256署名}"
// 形式で署名します。secretを知らない第三者は staffID や有効期限を改ざんしても正しい署名を
// 作成できないため、クッキー値の改ざん（なりすまし）を防げます。有効期限も署名対象に
// 含めることで、Max-Age（クライアント側の自己申告に過ぎない）が切れた後の値を
// 手動のCookieヘッダーで再送しても拒否できます。
// テストコードから正規のクッキー値を組み立てるために公開しています。
func SignStaffID(staffID uint, secret string, lifetime time.Duration) string {
	expiresAt := time.Now().Add(lifetime).Unix()
	payload := strconv.FormatUint(uint64(staffID), 10) + "." + strconv.FormatInt(expiresAt, 10)
	return payload + "." + hmacHex(payload, secret)
}

// verifyStaffID は署名済み staff_id クッキーの値を検証し、staffID を返します。
// 署名が不正・形式不正・有効期限切れの場合は 0（未認証）を返します。
func verifyStaffID(value string, secret string) uint {
	idPart, expPart, sig, ok := cutSignedValue(value)
	if !ok {
		return 0
	}
	payload := idPart + "." + expPart
	if !hmac.Equal([]byte(sig), []byte(hmacHex(payload, secret))) {
		return 0
	}
	id, err := strconv.ParseUint(idPart, 10, 32)
	if err != nil {
		return 0
	}
	expiresAt, err := strconv.ParseInt(expPart, 10, 64)
	if err != nil {
		return 0
	}
	if time.Now().Unix() > expiresAt {
		return 0
	}
	return uint(id)
}

// cutSignedValue は "{id}.{expiresAt}.{署名}" 形式の値を3つに分割します。
func cutSignedValue(value string) (idPart, expPart, sig string, ok bool) {
	parts := strings.SplitN(value, ".", 3)
	if len(parts) != 3 || parts[0] == "" || parts[1] == "" || parts[2] == "" {
		return "", "", "", false
	}
	return parts[0], parts[1], parts[2], true
}

// hmacHex は HMAC-SHA256(payload, secret) の16進数文字列を返します。
func hmacHex(payload string, secret string) string {
	mac := hmac.New(sha256.New, []byte(secret))
	mac.Write([]byte(payload))
	return hex.EncodeToString(mac.Sum(nil))
}
