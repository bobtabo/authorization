<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Gate\Repositories;

/**
 * Gate（JWT）向けキャッシュRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Gate
 */
interface GateRepository
{
    /**
     * JWT をキャッシュします。
     *
     * @param string $identifier クライアント識別名
     * @param string $memberId クライアント会員ID
     * @param string $token JWT 文字列
     * @param int $ttl 有効期限（秒）
     */
    public function putJwt(string $identifier, string $memberId, string $token, int $ttl): void;

    /**
     * キャッシュ済み JWT を取得します。
     *
     * @param string $identifier クライアント識別名
     * @param string $memberId クライアント会員ID
     * @return string|null キャッシュされた JWT 文字列、未キャッシュの場合 null
     */
    public function getJwt(string $identifier, string $memberId): ?string;
}
