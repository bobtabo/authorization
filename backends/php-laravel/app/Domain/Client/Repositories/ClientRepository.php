<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\Repositories;

use App\Domain\Client\Condition\ClientCondition;
use App\Domain\Client\Entities\Client;
use Illuminate\Support\Collection;

/**
 * クライアントを取得・条件検索・永続化するRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Repositories
 */
interface ClientRepository
{
    /**
     * クライアントリストを検索します。
     *
     * @param ClientCondition $condition 検索条件
     * @return Collection エンティティ
     */
    public function findByCondition(ClientCondition $condition): Collection;

    /**
     * クライアントを取得します。
     *
     * @param ClientCondition $condition 検索条件
     * @return Client|null エンティティ
     */
    public function findById(ClientCondition $condition): ?Client;

    /**
     * クライアントを新規登録または更新して永続化します。
     *
     * @param Client $entity エンティティ（id 未設定で新規）
     * @return Client 保存後のエンティティ
     */
    public function persist(Client $entity): Client;

    /**
     * アクセストークンでクライアントを取得します。
     *
     * @param ClientCondition $condition 検索条件
     * @return Client|null
     */
    public function findByAccessToken(ClientCondition $condition): ?Client;

    /**
     * クライアント識別名でクライアントを取得します。
     *
     * @param ClientCondition $condition 検索条件
     * @return Client|null エンティティ
     */
    public function findByIdentifier(ClientCondition $condition): ?Client;

    /**
     * クライアントを論理削除します。
     *
     * @param Client $entity エンティティ
     * @return bool 対象が存在して削除できた場合 true
     */
    public function deleteById(Client $entity): bool;
}
