<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Repositories;

use App\Support\Entity;
use App\Support\Exceptions\SystemException;
use App\Support\Models\AppModel;
use App\Support\Repositories\Cache\CacheKey;
use App\Support\Repositories\Cache\RedisModelCache;
use App\Support\Repositories\Conditions\Option;
use App\Support\Repositories\Traits\Bulk;
use App\Support\Repositories\Traits\CommonColumns;
use App\Support\Repositories\Traits\OptionBuilder;
use App\Support\Repositories\Traits\QueryLog;
use App\Traits\EnumValue;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Support\Collection;
use ReflectionClass;

/**
 * 基底Repositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories
 */
abstract class AbstractRepository
{
    use Bulk;
    use CommonColumns;
    use EnumValue;
    use OptionBuilder;
    use QueryLog;

    /**
     * 登録／更新します。
     *
     * @param \App\Support\Entity $entity エンティティ
     * @param bool $useGuarded $guarded値も含む場合 true を設定します
     * @return \App\Support\Entity 登録／更新されたエンティティ
     */
    public function save(Entity $entity, bool $useGuarded = false): Entity
    {
        $model = $this->getModel();
        $cache = CacheKey::getCacheKeyByModel($model);
        RedisModelCache::flush($cache);

        $entity = $this->assignCommons($entity, [
            $model->getCreatedAtColumn(),
            $model->getUpdatedAtColumn()
        ]);
        $attributes = $entity->attributesBySnake();
        $query = $model->newQuery();
        $query->where($model->getKeyName(), $attributes[$model->getKeyName()]);
        $model = $query->first();

        if (empty($model)) {
            //Insert
            $model = $this->getModel();
            if ($useGuarded) {
                $model = $this->assignWithGuarded($entity, $model);
            } else {
                $model = $model->fill($this->toValues($attributes));
            }
        } else {
            //Update
            $attributes = $this->assignCommonsByModel($attributes, $model);
            $model = $model->fill($this->toValues($attributes));
        }

        $model->save();

        return $this->getEntity()->assignModel($model);
    }

    /**
     * 論理削除します。
     *
     * @param int $id プライマリキー値
     * @param int|null $deletedId 削除実行者ID
     * @param string|null $column 検索カラム
     * @return bool 処理結果
     */
    public function delete(int $id, ?int $deletedId, ?string $column = null): bool
    {
        if (empty($deletedId)) {
            throw new SystemException(SystemException::GENERAL, [ '削除実行者IDが指定されていません。' ]);
        }

        $model = $this->getModel();
        $query = $model->newQuery();
        if (empty($column)) {
            $query->where($model->getKeyName(), '=', $id);
        } else {
            $query->where($column, '=', $id);
        }

        $models = $query->get();
        foreach ($models as $model) {
            $model->deleted_id = $deletedId;
            $model->delete();
        }

        $cache = CacheKey::getCacheKeyByModel($this->getModel());
        RedisModelCache::flush($cache);

        return true;
    }

    /**
     * 物理削除します。
     *
     * @param int $id プライマリキー値
     * @return bool 処理結果
     */
    public function forceDelete(int $id): bool
    {
        $model = $this->getModel();
        $query = $model->newQuery();
        $query->where($model->getKeyName(), '=', $id);

        $models = $query->get();
        foreach ($models as $model) {
            $model->forceDelete();
        }

        $cache = CacheKey::getCacheKeyByModel($this->getModel());
        RedisModelCache::flush($cache);

        return true;
    }

    /**
     * 全件リストを取得します。
     *
     * @param \App\Support\Repositories\Conditions\Option|null $option 検索オプション
     * @return \Illuminate\Support\Collection エンティティのコレクション
     */
    public function all(?Option $option = null): Collection
    {
        $model = $this->getModel();
        if (!empty($option) && $option->hasOrderBy()) {
            $result = $this->addOption($model->newQuery(), $option)->get();
        } else {
            $cache = CacheKey::getCacheKeyByModel($model);
            $result = RedisModelCache::get($cache);
            if (empty($result)) {
                $result = $this->addOption($model->newQuery(), $option)->get();
                RedisModelCache::put($cache, $result);
            }
        }

        return $this->assigns($result);
    }

    /**
     * 指定リレーションを含んだ全件リストを取得します。
     *
     * @param array<string, mixed> $relations リレーション=>リレーションクラスの連想配列
     * @param \App\Support\Repositories\Conditions\Option|null $option 検索オプション
     * @return \Illuminate\Support\Collection エンティティのコレクション
     */
    public function allWithRelation(array $relations, ?Option $option = null): Collection
    {
        $list = $this->all($option);

        if ($list->isEmpty()) {
            return $list;
        }

        return $list->map(function ($entity) use ($relations) {
            return $this->assignWithRelation($entity, $relations);
        });
    }

    /**
     * 件数を取得します。
     *
     * @param \Illuminate\Contracts\Database\Query\Builder|null $query クエリー
     * @return int 件数
     */
    public function count(?Builder $query = null): int
    {
        $model = $this->getModel();
        $query = empty($query) ? $model->newQuery() : $query;
        return $query->count();
    }

    /**
     * プライマリキーで検索します。
     *
     * @param int $id プライマリキー値
     * @param \Illuminate\Contracts\Database\Query\Builder|null $query クエリー
     * @return \App\Support\Entity|null エンティティ
     */
    public function findByPk(int $id, ?Builder $query = null): ?Entity
    {
        $model = $this->getModel();
        $cache = CacheKey::getCacheKeyByModel($model, [ $id ]);
        $modelValue = RedisModelCache::get($cache);

        if (empty($modelValue)) {
            if (empty($query)) {
                $query = $model->newQuery();
            }

            $query->where($model->getKeyName(), '=', $id);

            $modelValue = $query->first();
            RedisModelCache::put($cache, $modelValue);
        }

        if (is_null($modelValue)) {
            return null;
        }

        return $this->getEntity()->assignModel($modelValue);
    }

    /**
     * 複数値で検索します。
     *
     * @param array $values 検索値の配列
     * @param array<string, mixed> $relations リレーション=>リレーションクラスの連想配列
     * @param \Illuminate\Contracts\Database\Query\Builder|null $query クエリー
     * @param bool $not Not In で検索する場合 true を設定します
     * @return \Illuminate\Support\Collection エンティティのコレクション
     */
    public function findByIn(
        array $values,
        string $column = 'id',
        array $relations = [],
        ?Builder $query = null,
        bool $not = false
    ): Collection {
        $model = $this->getModel();
        if (empty($query)) {
            $query = $model->newQuery();
        }

        if ($not) {
            $query->whereNotIn($model->getTable() . '.' . $column, $values);
        } else {
            $query->whereIn($model->getTable() . '.' . $column, $values);
        }

        return $this->findByQuery($query, $relations);
    }

    /**
     * マップで検索します。
     *
     * @param array<string, mixed> $map カラム=>値の連想配列
     * @param \Illuminate\Contracts\Database\Query\Builder|null $query クエリー
     * @param \App\Support\Repositories\Conditions\Option|null $option 検索オプション
     * @return \Illuminate\Support\Collection エンティティのコレクション
     */
    public function findByMap(array $map, ?Builder $query = null, ?Option $option = null): Collection
    {
        $map = $this->toValues($map);
        $model = $this->getModel();
        $cache = CacheKey::getCacheKeyByModel($model, array_values($map));
        $modelValue = RedisModelCache::get($cache);

        if (empty($modelValue)) {
            if (empty($query)) {
                $query = $model->newQuery();
            }
            foreach ($map as $key => $value) {
                $query->where($key, '=', $this->toValue($value));
            }

            $modelValue = $this->addOption($query, $option)->get();
            RedisModelCache::put($cache, $modelValue);
        }

        return $this->assigns($modelValue);
    }

    /**
     * 指定リレーションを含んだエンティティをプライマリキーで検索します。
     *
     * @param int $id プライマリキー値
     * @param array<string, mixed> $relations リレーション=>リレーションクラスの連想配列
     * @param \Illuminate\Contracts\Database\Eloquent\Builder|null $query クエリー
     * @return \App\Support\Entity|null エンティティ
     */
    public function findByPkWithRelation(int $id, array $relations, ?Builder $query = null): ?Entity
    {
        $entity = $this->findByPk($id, $query);
        if (empty($entity)) {
            return $entity;
        }

        return $this->assignWithRelation($entity, $relations);
    }

    /**
     * 指定リレーションを含んだエンティティをマップで検索します。
     *
     * @param array<string, mixed> $map カラム=>値の連想配列
     * @param array<string, mixed> $relations リレーション=>リレーションクラスの連想配列
     * @param \Illuminate\Contracts\Database\Eloquent\Builder|null $query クエリー
     * @param \App\Support\Repositories\Conditions\Option|null $option 検索オプション
     * @return \Illuminate\Support\Collection エンティティのコレクション
     */
    public function findByMapWithRelation(
        array $map,
        array $relations,
        ?Builder $query = null,
        ?Option $option = null
    ): Collection {
        $list = $this->findByMap($map, $query, $option);
        if ($list->isEmpty()) {
            return $list;
        }

        return $list->map(function ($entity) use ($relations) {
            return $this->assignWithRelation($entity, $relations);
        });
    }

    /**
     * テーブル名を取得します。
     *
     * @return string テーブル名
     */
    public function getTable(): string
    {
        return $this::getModel()->getTable();
    }

    /**
     * 指定クエリーで検索します。
     *
     * @param \Illuminate\Contracts\Database\Eloquent\Builder $query クエリー
     * @param array<string, mixed> $relations リレーション=>リレーションクラスの連想配列
     * @return \Illuminate\Support\Collection エンティティのコレクション
     */
    protected function findByQuery(Builder $query, array $relations = []): Collection
    {
        $keys = array_keys($relations);
        $list = empty($keys) ? $query->get() : $query->with($keys)->get();
        if ($list->isEmpty()) {
            return $list;
        }

        $excludes = [];
        foreach ($keys as $key) {
            $excludes[] = str($key)->snake()->value();
        }
        $result = $this->assigns($list, $excludes);
        if (empty($relations)) {
            return $result;
        }

        return $result->map(function ($entity) use ($relations) {
            return $this->assignWithRelation($entity, $relations);
        });
    }

    /**
     * 指定クエリーで削除します。
     *
     * @param \Illuminate\Contracts\Database\Eloquent\Builder $query クエリー
     * @return bool 処理結果
     * @throws \Psr\SimpleCache\InvalidArgumentException キャッシュ例外
     */
    protected function deleteByQuery(Builder $query): bool
    {
        $models = $query->get();
        foreach ($models as $model) {
            $model->delete();
        }

        $cache = CacheKey::getCacheKeyByModel($models->first(), []);
        RedisModelCache::flush($cache);

        return true;
    }

    /**
     * モデルリストをエンティティのリストに変換します。
     *
     * @param \Illuminate\Support\Collection $models モデルのリスト
     * @param array<string> $excludes 除外項目
     * @return \Illuminate\Support\Collection エンティティのリスト
     */
    protected function assigns(Collection $models, array $excludes = []): Collection
    {
        return $models->map(function (AppModel $model) use ($excludes) {
            return $this->getEntity()->assign($model->toArray(), [], $excludes);
        });
    }

    /**
     * 対象モデルに $guarded を含むデータ設定します。
     *
     * @param \App\Support\Entity $entity エンティティ
     * @param AppModel|null $model 対象モデル
     * @return AppModel データ設定したモデル
     */
    protected function assignWithGuarded(Entity $entity, ?AppModel $model = null): AppModel
    {
        if (empty($model)) {
            $model = $this->getModel();
        }
        $model->fill($entity->attributesBySnake());
        $guardeds = $model->getGuarded();
        foreach ($guardeds as $guarded) {
            $model->$guarded = $entity->attributesBySnake()[$guarded];
        }
        return $model;
    }

    /**
     * 対象エンティティにリレーションを設定します。
     *
     * @param \App\Support\Entity $entity 対象エンティティ
     * @param array<string, mixed> $relations リレーション=>リレーションクラスの連想配列
     * @return \App\Support\Entity リレーション設定したエンティティ
     */
    protected function assignWithRelation(Entity $entity, array $relations): Entity
    {
        $model = $this->assignWithGuarded($entity);
        foreach ($relations as $relation => $relationClass) {
            $relationModel = $model->$relation;
            /** @var Entity $relationEntity */
            if ($relationModel instanceof Collection) {
                $relationEntities = $relationModel->map(function (AppModel $model) use ($relationClass) {
                    $relationEntity = new $relationClass;
                    return $relationEntity->assign($model->toArray());
                });
                $entity->$relation = $relationEntities;
            } else {
                if (!empty($relationModel)) {
                    $relationEntity = new $relationClass;
                    $relationEntity->assignModel($relationModel);
                    $modelName = str((new ReflectionClass($relationModel))->getShortName())->camel()->value();
                    if ($modelName === $relation) {
                        $entity->$relation = $relationEntity;
                    } else {
                        $entity->$modelName = $relationEntity;
                    }
                }
            }
        }
        return $entity;
    }

    /**
     * モデルを取得します。
     *
     * @return AppModel モデル
     */
    abstract protected function getModel(): AppModel;

    /**
     * エンティティを取得します。
     *
     * @return \App\Support\Entity エンティティ
     */
    abstract protected function getEntity(): Entity;
}
