"""
汎用オブジェクトマッピングモジュール。

Author: Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
"""
from dataclasses import fields as dc_fields
from typing import TypeVar

T = TypeVar("T")


def assign(dst: T, src: object, *, convert: dict[str, str] | None = None, excludes: list[str] | None = None) -> T:
    """ソースオブジェクトからデストオブジェクトへ同名フィールドをコピーします。

    デストの dataclass フィールドを走査し、ソースに同名属性があればセットします。
    ソースに存在しないフィールドはデストのデフォルト値を保持します。

    Args:
        dst: コピー先オブジェクト（dataclass インスタンス）
        src: コピー元オブジェクト
        convert: フィールド名変換マップ {dst_field: src_field}
        excludes: 除外するデストフィールド名のリスト

    Returns:
        dst
    """
    convert = convert or {}
    excludes = excludes or []
    for f in dc_fields(dst):
        if f.name in excludes:
            continue
        src_attr = convert.get(f.name, f.name)
        if hasattr(src, src_attr):
            setattr(dst, f.name, getattr(src, src_attr))
    return dst
