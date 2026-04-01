<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
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
     * @param ClientCondition $condition
     * @return Collection
     */
    public function findByCondition(ClientCondition $condition): Collection;

    /**
     * クライアントを取得します。
     *
     * @param ClientCondition $condition
     * @return Client|null
     */
    public function findById(ClientCondition $condition): ?Client;

    /**
     * クライアントを新規登録または更新して永続化します。
     *
     * {@see \App\Support\Repositories\AbstractRepository::save} とは別シグネチャのため persist とします。
     *
     * @param  Client  $entity  永続化するエンティティ（id 未設定で新規）
     * @return Client 保存後のエンティティ
     */
    public function persist(Client $entity): Client;

    /**
     * クライアントを論理削除します。
     *
     * @param  int  $id  クライアントID
     * @param  int  $executorId  処理実行者ID
     * @return bool 対象が存在して削除できた場合 true
     */
    public function deleteById(int $id, int $executorId): bool;
}
