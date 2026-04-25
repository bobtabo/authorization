"""
アプリケーション例外モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""


class AppError(Exception):
    """アプリケーション例外。HTTP ステータスコードとメッセージを保持します。"""

    def __init__(self, status_code: int, message: str):
        """初期化します。

        Args:
            status_code: HTTP ステータスコード
            message: エラーメッセージキー
        """
        self.status_code = status_code
        self.message = message


def bad_request(message: str = "bad_request") -> AppError:
    """400 Bad Request 例外を返します。

    Args:
        message: エラーメッセージキー

    Returns:
        AppError インスタンス
    """
    return AppError(400, message)


def unauthorized(message: str = "unauthenticated") -> AppError:
    """401 Unauthorized 例外を返します。

    Args:
        message: エラーメッセージキー

    Returns:
        AppError インスタンス
    """
    return AppError(401, message)


def forbidden(message: str = "forbidden") -> AppError:
    """403 Forbidden 例外を返します。

    Args:
        message: エラーメッセージキー

    Returns:
        AppError インスタンス
    """
    return AppError(403, message)


def not_found(message: str = "not_found") -> AppError:
    """404 Not Found 例外を返します。

    Args:
        message: エラーメッセージキー

    Returns:
        AppError インスタンス
    """
    return AppError(404, message)


def conflict(message: str = "conflict") -> AppError:
    """409 Conflict 例外を返します。

    Args:
        message: エラーメッセージキー

    Returns:
        AppError インスタンス
    """
    return AppError(409, message)


def internal(message: str = "internal_server_error") -> AppError:
    """500 Internal Server Error 例外を返します。

    Args:
        message: エラーメッセージキー

    Returns:
        AppError インスタンス
    """
    return AppError(500, message)
