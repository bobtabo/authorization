<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\JwtIssueHistory\Repositories;

use App\Domain\JwtIssueHistory\Condition\JwtIssueHistoryCondition;
use App\Domain\JwtIssueHistory\Entities\JwtIssueHistory;
use Illuminate\Support\Collection;

/**
 * JWT発行履歴Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\JwtIssueHistory\Repositories
 */
interface JwtIssueHistoryRepository
{
    /**
     * クライアントIDに紐づくJWT発行履歴を返します。
     *
     * @param JwtIssueHistoryCondition $condition 検索条件
     * @return Collection エンティティコレクション
     */
    public function findByClientId(JwtIssueHistoryCondition $condition): Collection;

    /**
     * JWT発行履歴を保存します。
     *
     * @param JwtIssueHistory $entity エンティティ
     * @return JwtIssueHistory 保存後のエンティティ
     */
    public function persist(JwtIssueHistory $entity): JwtIssueHistory;
}
