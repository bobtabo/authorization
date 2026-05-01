<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Invitation\Repositories;

/**
 * 招待認証Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Invitation\Repositories
 */
interface InvitationAuthRepository
{
    /**
     * 招待トークンを一時保存します。
     *
     * @param string $token 招待トークン
     * @param int $ttl 有効期限（秒）
     */
    public function store(string $token, int $ttl): void;

    /**
     * 招待トークンを取得します。
     *
     * @param string $token 招待トークン
     * @return string|null トークン文字列、未保存の場合 null
     */
    public function find(string $token): ?string;

    /**
     * 招待トークンを削除します。
     *
     * @param string $token 招待トークン
     */
    public function remove(string $token): void;
}
