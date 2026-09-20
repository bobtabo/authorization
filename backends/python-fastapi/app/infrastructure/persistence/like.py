"""LIKE検索用のエスケープ処理モジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""


def escape_like_keyword(keyword: str) -> str:
    """LIKE検索用にキーワードをエスケープします。

    キーワードに含まれる `\\`/`%`/`_` はLIKEパターン上でエスケープ文字・ワイルドカードとして
    解釈されるため、リテラル文字列として一致させるには事前にエスケープする必要がある。
    `\\` は他の文字のエスケープに使うため最初に変換する。

    Args:
        keyword: エスケープ対象のキーワード

    Returns:
        エスケープ済みのキーワード
    """
    return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
