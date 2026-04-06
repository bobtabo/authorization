<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Repositories\Traits;

use App\Support\Entity;
use Arr;
use Carbon\Carbon;
use Illuminate\Database\Eloquent\Model;
use ReflectionClass;
use Str;

/**
 * 共通カラムTraitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Traits
 */
trait CommonColumns
{
    /**
     * エンティティ共通カラムを設定します。
     *
     * @param Entity $entity エンティティ
     * @param array $dateColumns タイムスタンプカラム配列
     * @return Entity 設定したエンティティ
     */
    protected function assignCommons(Entity $entity, array $dateColumns): Entity
    {
        $entityClass = new ReflectionClass($entity);

        foreach ($dateColumns as $column) {
            $camel = Str::camel($column);
            if ($entityClass->hasProperty($camel) && empty($entity->$camel)) {
                $entity->assign([ $camel => Carbon::now() ]);
            }
        }

        if ($entityClass->hasProperty('deletedAt')) {
            $entity->deletedAt = null;
        }

        if ($entityClass->hasProperty('version')) {
            $entity->version = empty($entity->version) ? 1 : $entity->version;
        }

        return $entity;
    }

    /**
     * モデル共通カラムをエンティティに設定します。
     *
     * @param array $entityAttributes エンティティ属性
     * @param Model $model モデル
     * @return array 設定したエンティティ属性
     */
    protected function assignCommonsByModel(array $entityAttributes, Model $model): array
    {
        $columns = [
            $model->getCreatedAtColumn(),
            $model->getUpdatedAtColumn(),
            'version',
        ];

        foreach ($columns as $column) {
            if (Arr::exists($entityAttributes, $column)) {
                $entityAttributes[$column] = $model->$column;
            }
        }

        $entityAttributes[$model->getDeletedAtColumn()] = null;

        return $entityAttributes;
    }
}
