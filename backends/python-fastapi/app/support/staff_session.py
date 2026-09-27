"""
staff_id クッキーの署名・検証モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
import hashlib
import hmac
import time


def sign_staff_id(staff_id: int, secret: str, lifetime_seconds: int) -> str:
    """staff_id クッキーの値を "{staffId}.{有効期限のUnix秒}.{HMAC-SHA256署名}" 形式で署名する。

    secretを知らない第三者は staffId や有効期限を改ざんしても正しい署名を作成できないため、
    クッキー値の改ざん（なりすまし）を防げる。有効期限も署名対象に含めることで、
    Max-Age（クライアント側の自己申告に過ぎない）が切れた後の値を手動のCookieヘッダーで
    再送しても拒否できる。
    """
    expires_at = int(time.time()) + lifetime_seconds
    payload = f"{staff_id}.{expires_at}"
    return f"{payload}.{_hmac_hex(payload, secret)}"


def verify_staff_id(value: str | None, secret: str) -> int:
    """署名済み staff_id クッキーの値を検証し、staff_idを返す。

    署名が不正・形式不正・有効期限切れの場合は 0（未認証）を返す。
    """
    if not value:
        return 0

    parts = value.split(".", 2)
    if len(parts) != 3 or not all(parts):
        return 0
    id_part, exp_part, sig = parts

    payload = f"{id_part}.{exp_part}"
    expected_sig = _hmac_hex(payload, secret)
    if not hmac.compare_digest(sig.encode(), expected_sig.encode()):
        return 0

    if not id_part.isdigit() or not exp_part.isdigit():
        return 0
    if int(time.time()) > int(exp_part):
        return 0

    return int(id_part)


def _hmac_hex(payload: str, secret: str) -> str:
    """HMAC-SHA256(payload, secret) の16進数文字列を返す。"""
    return hmac.new(secret.encode(), payload.encode(), hashlib.sha256).hexdigest()
