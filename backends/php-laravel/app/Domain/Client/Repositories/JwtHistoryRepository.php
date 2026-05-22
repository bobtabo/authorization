<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\Repositories;

use App\Domain\Client\Condition\JwtHistoryCondition;
use App\Domain\Client\Entities\JwtHistory;
use Illuminate\Support\Collection;

/**
 * JWT履歴Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Repositories
 */
interface JwtHistoryRepository
{
    /**
     * クライアントIDに紐づくJWT履歴を返します。
     *
     * @param JwtHistoryCondition $condition 検索条件
     * @return Collection エンティティコレクション
     */
    public function findByClientId(JwtHistoryCondition $condition): Collection;

    /**
     * JWT履歴を保存します。
     *
     * @param JwtHistory $entity エンティティ
     * @return JwtHistory 保存後のエンティティ
     */
    public function persist(JwtHistory $entity): JwtHistory;
}
