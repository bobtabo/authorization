<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Persistence;

/**
 * LIKE検索用のエスケープ処理クラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence
 */
class LikeEscaper
{
    /**
     * LIKE検索用にキーワードをエスケープします。
     *
     * キーワードに含まれる `\`/`%`/`_` はLIKEパターン上でエスケープ文字・ワイルドカードとして
     * 解釈されるため、リテラル文字列として一致させるには事前にエスケープする必要がある。
     * `\` は他の文字のエスケープに使うため最初に変換する。
     *
     * @param  string  $keyword  エスケープ対象のキーワード
     * @return string エスケープ済みのキーワード
     */
    public static function escape(string $keyword): string
    {
        return str_replace(['\\', '%', '_'], ['\\\\', '\\%', '\\_'], $keyword);
    }
}
