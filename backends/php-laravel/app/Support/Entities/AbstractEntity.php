<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Entities;

use App\Support\Entity;
use App\Support\Traits\Assign;
use App\Support\Traits\Attribute;
use App\Support\Traits\Initialize;
use Carbon\Carbon;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Collection;
use ReflectionClass;
use ReflectionException;
use ReflectionProperty;
use Str;

/**
 * 基底Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain
 */
abstract class AbstractEntity implements Entity
{
    use Assign, Attribute, Initialize;

    public ?Carbon $createdAt = null;
    public ?int $createdBy = null;
    public ?Carbon $updatedAt = null;
    public ?int $updatedBy = null;
    public ?Carbon $deletedAt = null;
    public ?int $deletedBy = null;
    public ?int $version = null;

    /**
     * コンストラクタ
     */
    public function __construct()
    {
        $this->initializer();
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function assignModel(Model $model): Entity
    {
        $attributes = $model->getAttributes();
        return $this->assign($attributes);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function attributes(): array
    {
        $result = [];

        $clazz = new ReflectionClass($this);
        $properties = $clazz->getProperties();
        foreach ($properties as $property) {
            //リレーションプロパティを除外します
            if ($this->isRelation($property)) {
                continue;
            }

            $key = $property->getName();
            $result[$key] = $property->getValue($this);
        }

        return $result;
    }

    /**
     * スネーク属性を取得します。
     *
     * @return array<string, mixed> 属性の配列
     */
    public function attributesBySnake(): array
    {
        $result = [];
        $clazz = new ReflectionClass($this);
        $properties = $clazz->getProperties();
        foreach ($properties as $property) {
            //リレーションプロパティを除外します
            if ($this->isRelation($property)) {
                continue;
            }

            $key = $property->getName();
            $snake = str($key)->snake()->value();
            $result[$snake] = $property->getValue($this);
        }
        return $result;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function assignCreatedSystem(): Entity
    {
        return $this->assignCreated(0);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function assignCreated(int $executorId): Entity
    {
        $now = Carbon::now();
        $this->createdAt = $now;
        $this->createdBy = $executorId;
        $this->updatedAt = $now;
        $this->updatedBy = $executorId;
        $this->version = 1;
        return $this;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function assignUpdatedSystem(): Entity
    {
        return $this->assignUpdated(0);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function assignUpdated(int $executorId): Entity
    {
        $this->updatedAt = Carbon::now();
        $this->updatedBy = $executorId;
        return $this;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function assignDeletedSystem(): Entity
    {
        return $this->assignDeleted(0);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function assignDeleted(int $executorId): Entity
    {
        $this->deletedAt = Carbon::now();
        $this->deletedBy = $executorId;
        return $this;
    }

    /**
     * リレーションプロパティであるか確認します。
     *
     * @param ReflectionProperty $property プロパティ
     * @return bool リレーションプロパティの場合 true を返します
     */
    protected function isRelation(ReflectionProperty $property): bool
    {
        $result = false;
        $typeClass = $property->getType()->getName();

        //Collectionの場合はhasManyプロパティなので true を返します
        if ($typeClass == Collection::class) {
            return true;
        }

        try {
            $interfaces = (new ReflectionClass($typeClass))->getInterfaceNames();
            foreach ($interfaces as $interface) {
                if ($interface == Entity::class) {
                    $result = true;
                    break;
                }
            }
        } catch (ReflectionException $e) {
            //リレーションプロパティではない場合、getInterfaceNames() で例外になるので無視します
        }

        return $result;
    }
}
