<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Client\Entities\Client;
use App\Domain\Client\Enums\ClientStatus;
use App\Domain\Client\Repositories\ClientRepositoryInterface;
use App\Infrastructure\Persistence\Eloquent\Models\Client as Model;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\QueryException;

/**
 * Eloquent によりクライアントを読み取るRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
final class EloquentClientRepository extends AbstractRepository implements ClientRepositoryInterface
{
    /**
     * @param  Model  $model  クライアント Eloquent モデル
     */
    public function __construct(
        private readonly Model $model,
    ) {}

    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     */
    #[\Override]
    public function findById(int $id): ?Client
    {
        $row = $this->model->newQuery()->find($id);

        if ($row === null) {
            return null;
        }

        return $this->toEntity($row);
    }

    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     */
    #[\Override]
    public function search(
        ?string $keyword = null,
        ?string $startFrom = null,
        ?string $startTo = null,
        array $statuses = [],
    ): array {
        $q = $this->model->newQuery();

        if ($keyword !== null && $keyword !== '') {
            $q->where(function (Builder $b) use ($keyword): void {
                $b->where('name', 'like', '%'.$keyword.'%');
            });
        }

        if ($startFrom !== null && $startFrom !== '') {
            $q->whereDate('start_at', '>=', $startFrom);
        }

        if ($startTo !== null && $startTo !== '') {
            $q->whereDate('start_at', '<=', $startTo);
        }

        if ($statuses !== []) {
            $q->whereIn('status', $statuses);
        }

        return $q->get()->map(fn (Model $row): Client => $this->toEntity($row))->all();
    }

    /**
     * Eloquent 行をドメイン Entity に変換します。
     *
     * @param  Model  $row  Eloquent クライアント行
     * @return Client ドメインのクライアント
     */
    private function toEntity(Model $row): Client
    {
        $status = ClientStatus::tryFrom((int) ($row->status ?? 0)) ?? ClientStatus::Inactive;

        return new Client(
            id: (int) $row->getKey(),
            name: (string) ($row->name ?? ''),
            identifier: (string) ($row->identifer ?? $row->identifier ?? ''),
            status: $status,
        );
    }
}
