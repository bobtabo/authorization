<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Client;

use App\Domain\Client\Entities\Client;
use App\Domain\Client\Repositories\ClientRepositoryInterface;
use App\UseCases\Common\AbstractService;
use Illuminate\Database\QueryException;

/**
 * クライアントの取得・一覧検索のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Client
 */
final class ClientApplicationService extends AbstractService
{
    /**
     * @param  ClientRepositoryInterface  $clients  クライアントRepository
     */
    public function __construct(
        private readonly ClientRepositoryInterface $clients,
    ) {}

    /**
     * ID でクライアントを1件取得します。
     *
     * @param  int  $id  クライアントID
     * @return Client|null 該当がなければ null
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function get(int $id): ?Client
    {
        return $this->clients->findById($id);
    }

    /**
     * 条件でクライアント一覧を取得します。
     *
     * @param  string|null  $keyword  名前向けキーワード（null または空は無条件）
     * @param  string|null  $startFrom  利用開始日 From（Y-m-d 相当、null または空は無条件）
     * @param  string|null  $startTo  利用開始日 To（Y-m-d 相当、null または空は無条件）
     * @param  array<int, int>  $statuses  状態コードの一覧（空は無条件）
     * @return list<Client> クライアントのリスト
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function list(
        ?string $keyword = null,
        ?string $startFrom = null,
        ?string $startTo = null,
        array $statuses = [],
    ): array {
        return $this->clients->search($keyword, $startFrom, $startTo, $statuses);
    }
}
