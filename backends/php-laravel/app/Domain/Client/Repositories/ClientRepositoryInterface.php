<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Repositories;

use App\Domain\Client\Entities\Client;

/**
 * クライアントを取得・条件検索するRepositoryのインターフェースです。
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
}
