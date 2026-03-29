<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Account\Entities\Account;
use App\Domain\Account\Repositories\AccountRepositoryInterface;
use App\Infrastructure\Persistence\Eloquent\Models\AdminAccountModel;
use Illuminate\Database\Eloquent\Builder;

/**
 * Eloquent によりアカウントを読み取るRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
final class EloquentAccountRepository implements AccountRepositoryInterface
{
    public function __construct(
        private readonly AdminAccountModel $model,
    ) {}

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findById(int $id): ?Account
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
        array $roles = [],
        array $statuses = [],
    ): array {
        $q = $this->model->newQuery();

        if ($keyword !== null && $keyword !== '') {
            $q->where(function (Builder $b) use ($keyword): void {
                $b->where('name', 'like', '%'.$keyword.'%')
                    ->orWhere('email', 'like', '%'.$keyword.'%');
            });
        }

        if ($roles !== []) {
            $q->whereIn('role', $roles);
        }

        if ($statuses !== []) {
            $q->whereIn('active', $statuses);
        }

        return $q->get()->map(fn (AdminAccountModel $row): Account => $this->toEntity($row))->all();
    }

    private function toEntity(AdminAccountModel $row): Account
    {
        return new Account(
            id: (int) $row->getKey(),
            name: (string) ($row->name ?? ''),
            email: (string) ($row->email ?? ''),
            activeValue: (int) ($row->active ?? 0),
            roleValue: (int) ($row->role ?? 0),
        );
    }
}
