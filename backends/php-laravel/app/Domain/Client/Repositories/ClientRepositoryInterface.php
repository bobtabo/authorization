<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Repositories;

use App\Domain\Client\Condition\ClientCondition;
use App\Domain\Client\Entities\Client;

/**
 * クライアントを取得・条件検索・永続化するRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Repositories
 */
interface ClientRepositoryInterface
{
    /**
     * ID でクライアントを1件取得します。
     *
     * @param  int  $id  クライアントID
     * @return Client|null 該当がなければ null
     */
    public function findById(int $id): ?Client;

    /**
     * 条件でクライアントを検索します。
     *
     * @param  string|null  $keyword  名前向けキーワード（null または空は無条件）
     * @param  string|null  $startFrom  利用開始日 From（null または空は無条件）
     * @param  string|null  $startTo  利用開始日 To（null または空は無条件）
     * @param  array<int, int>  $statuses  状態コードの一覧（空は無条件）
     * @return list<Client> クライアントのリスト
     */
    public function search(
        ?string $keyword = null,
        ?string $startFrom = null,
        ?string $startTo = null,
        array $statuses = [],
    ): array;

    /**
     * 条件オブジェクトでクライアントを検索します。
     *
     * @return list<Client>
     */
    public function searchByCondition(ClientCondition $condition): array;

    /**
     * 条件オブジェクトでクライアントを1件取得します（主に id 指定）。
     */
    public function findByCondition(ClientCondition $condition): ?Client;

    /**
     * クライアントを新規登録または更新して永続化します。
     *
     * {@see \App\Support\Repositories\AbstractRepository::save} とは別シグネチャのため persist とします。
     *
     * @param  Client  $entity  永続化するエンティティ（id 未設定で新規）
     * @param  int|null  $executorId  登録／更新実行者ID（未ログイン等は null。永続化層で必要なら 0 に正規化）
     * @return Client 保存後のエンティティ
     */
    public function persist(Client $entity, ?int $executorId = null): Client;

    /**
     * クライアントを論理削除します。
     *
     * @param  int  $id  クライアントID
     * @param  int|null  $executorId  削除実行者ID（未ログイン等は null）
     * @return bool 対象が存在して削除できた場合 true
     */
    public function softDelete(int $id, ?int $executorId = null): bool;
}
