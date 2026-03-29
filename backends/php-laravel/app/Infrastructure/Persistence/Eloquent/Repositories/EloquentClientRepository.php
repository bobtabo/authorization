<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Client\Entities\Client;
use App\Domain\Client\Repositories\ClientRepositoryInterface;
use App\Infrastructure\Persistence\Eloquent\Models\ClientModel;
use Illuminate\Database\Eloquent\Builder;

/**
 * Eloquent によりクライアントを読み取るRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
final class EloquentClientRepository implements ClientRepositoryInterface
{
    public function __construct(
        private readonly ClientModel $model,
    ) {}

    /**
     * {@inheritdoc}
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

        return $q->get()->map(fn (ClientModel $row): Client => $this->toEntity($row))->all();
    }

    private function toEntity(ClientModel $row): Client
    {
        return new Client(
            id: (int) $row->getKey(),
            name: (string) ($row->name ?? ''),
            identifier: (string) ($row->identifier ?? ''),
            status: (int) ($row->status ?? 0),
        );
    }
}
