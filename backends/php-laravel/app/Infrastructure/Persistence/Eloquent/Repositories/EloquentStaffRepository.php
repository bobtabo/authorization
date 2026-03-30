<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Staff\Entities\Staff;
use App\Domain\Staff\Enums\StaffRole;
use App\Domain\Staff\Enums\StaffStatus;
use App\Domain\Staff\Repositories\StaffRepositoryInterface;
use App\Infrastructure\Persistence\Eloquent\Models\Staff as Model;
use app\Support\Repositories\AbstractRepository;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\QueryException;

/**
 * Eloquent によりスタッフを読み取るRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
class EloquentStaffRepository extends AbstractRepository implements StaffRepositoryInterface
{
    /**
     * @param  Model  $model  スタッフ Eloquent モデル
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
    public function findById(int $id): ?Staff
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
            $q->whereIn('status', $statuses);
        }

        return $q->get()->map(fn (Model $row): Staff => $this->toEntity($row))->all();
    }

    /**
     * Eloquent 行をドメイン Entity に変換します。
     *
     * @param  Model  $row  Eloquent スタッフ行
     * @return Staff ドメインのスタッフ
     */
    private function toEntity(Model $row): Staff
    {
        $status = StaffStatus::tryFrom((int) ($row->status ?? 1)) ?? StaffStatus::Active;
        $role = StaffRole::tryFrom((int) ($row->role ?? 1)) ?? StaffRole::Member;

        return new Staff(
            id: (int) $row->getKey(),
            name: (string) ($row->name ?? ''),
            email: (string) ($row->email ?? ''),
            status: $status,
            role: $role,
        );
    }
}
