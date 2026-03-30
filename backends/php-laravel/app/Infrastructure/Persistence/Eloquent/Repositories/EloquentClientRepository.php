<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Client\Condition\ClientCondition;
use App\Domain\Client\Entities\Client;
use App\Domain\Client\Enums\ClientStatus;
use App\Domain\Client\Repositories\ClientRepositoryInterface;
use App\Infrastructure\Persistence\Eloquent\Models\Client as ClientModel;
use App\Support\Entity;
use App\Support\Models\AppModel;
use App\Support\Repositories\AbstractRepository;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Database\QueryException;

/**
 * Eloquent によりクライアントを永続化するRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
class EloquentClientRepository extends AbstractRepository implements ClientRepositoryInterface
{
    /**
     * @param  ClientModel  $model  クライアント Eloquent モデル
     */
    public function __construct(
        private readonly ClientModel $model,
    ) {}

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getModel(): AppModel
    {
        return $this->model;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getEntity(): Entity
    {
        return new Client;
    }

    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     */
    #[\Override]
    public function findById(int $id): ?Client
    {
        $row = $this->baseQuery()->find($id);

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
        $condition = new ClientCondition;
        $condition->keyword = $keyword;
        $condition->startFrom = $startFrom;
        $condition->startTo = $startTo;
        $condition->statuses = $statuses;

        return $this->searchByCondition($condition);
    }

    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     */
    #[\Override]
    public function searchByCondition(ClientCondition $condition): array
    {
        $q = $this->baseQuery();

        $keyword = $condition->keyword;
        if ($keyword !== null && $keyword !== '') {
            $kw = '%'.$keyword.'%';
            $q->where(function (Builder $b) use ($kw): void {
                $b->where('name', 'like', $kw)
                    ->orWhere('identifer', 'like', $kw);
            });
        }

        $startFrom = $condition->startFrom;
        if ($startFrom !== null && $startFrom !== '') {
            $q->whereDate('start_at', '>=', $startFrom);
        }

        $startTo = $condition->startTo;
        if ($startTo !== null && $startTo !== '') {
            $q->whereDate('start_at', '<=', $startTo);
        }

        if ($condition->statuses !== []) {
            $q->whereIn('status', $condition->statuses);
        }

        return $q->orderByDesc('id')
            ->get()
            ->map(fn (ClientModel $row): Client => $this->toEntity($row))
            ->all();
    }

    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     */
    #[\Override]
    public function findByCondition(ClientCondition $condition): ?Client
    {
        if ($condition->id === null || $condition->id === 0) {
            return null;
        }

        return $this->findById($condition->id);
    }

    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     * @throws ModelNotFoundException 更新対象が存在しない場合
     */
    #[\Override]
    public function persist(Client $entity, ?int $executorId = null): Client
    {
        $by = $executorId ?? 0;

        $model = $entity->id !== null && $entity->id !== 0
            ? $this->baseQuery()->find($entity->id)
            : null;

        if ($entity->id !== null && $entity->id !== 0 && $model === null) {
            throw (new ModelNotFoundException)->setModel(ClientModel::class, [$entity->id]);
        }

        if ($model === null) {
            $model = $this->model->newInstance();
        }

        $fill = $this->fillAttributesFromEntity($entity, $model);

        if ($entity->id === null || $entity->id === 0) {
            $fill = array_merge($this->defaultsForInsert(), $fill);
            $fill['created_by'] = $by;
            $fill['version'] = 1;
        } else {
            $fill['version'] = (int) ($model->getAttribute('version') ?? 1) + 1;
        }

        $fill['updated_by'] = $by;

        $model->fill($fill);
        $model->save();

        $fresh = $model->fresh();
        if ($fresh === null) {
            throw (new ModelNotFoundException)->setModel(ClientModel::class, [$model->getKey()]);
        }

        return $this->toEntity($fresh);
    }

    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     */
    #[\Override]
    public function softDelete(int $id, ?int $executorId = null): bool
    {
        $row = $this->baseQuery()->find($id);
        if ($row === null) {
            return false;
        }

        $row->setAttribute('deleted_by', $executorId ?? 0);
        $row->delete();

        return true;
    }

    /**
     * 論理削除されていない行のみを対象とするクエリを返します。
     *
     * @return Builder<ClientModel>
     */
    private function baseQuery(): Builder
    {
        return $this->model->newQuery()->active();
    }

    /**
     * @return array<string, mixed>
     */
    private function defaultsForInsert(): array
    {
        return [
            'access_token' => '',
            'private_key' => '',
            'public_key' => '',
            'fingerprint' => '',
            'status' => ClientStatus::Active->value,
        ];
    }

    /**
     * @param  ClientModel  $model  更新時は既存行（version などに利用）
     * @return array<string, mixed>
     */
    private function fillAttributesFromEntity(Client $entity, ClientModel $model): array
    {
        $map = [
            'name' => $entity->name,
            'identifer' => $entity->identifer,
            'post_code' => $entity->postCode,
            'pref' => $entity->pref,
            'city' => $entity->city,
            'address' => $entity->address,
            'building' => $entity->building,
            'tel' => $entity->tel,
            'email' => $entity->email,
            'access_token' => $entity->accessToken,
            'private_key' => $entity->privateKey,
            'public_key' => $entity->publicKey,
            'fingerprint' => $entity->fingerprint,
            'status' => $entity->status instanceof ClientStatus ? $entity->status->value : $entity->status,
            'start_at' => $entity->startAt,
            'stop_at' => $entity->stopAt,
        ];

        $result = [];
        foreach ($map as $column => $value) {
            if ($value !== null) {
                $result[$column] = $value;
            }
        }

        return $result;
    }

    private function toEntity(ClientModel $row): Client
    {
        $entity = new Client;

        return $entity->assignModel($row);
    }
}
