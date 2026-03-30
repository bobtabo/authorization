<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Models;

use App\Support\Models\AppMasterModel;
use Database\Factories\ClientFactory;
use Illuminate\Database\Eloquent\Builder;

/**
 * クライアントModelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Models
 */
class Client extends AppMasterModel
{
    protected $table = 'clients';

    protected $guarded = [];

    /**
     * {@inheritdoc}
     */
    protected static function newFactory()
    {
        return ClientFactory::new();
    }

    /**
     * 論理削除済み行を除外するスコープです。
     *
     * @param  Builder<self>  $query
     * @return Builder<self>
     */
    public function scopeActive(Builder $query): Builder
    {
        return $query->whereNull('deleted_at');
    }

    /**
     * clients テーブルは deleted_by のみ保持し、親の deleted_type / deleted_id 更新は行いません。
     */
    #[\Override]
    protected function runSoftDelete(): void
    {
        $query = $this->setKeysForSaveQuery($this->newModelQuery());

        $time = $this->freshTimestamp();

        $columns = [$this->getDeletedAtColumn() => $this->fromDateTime($time)];

        $this->{$this->getDeletedAtColumn()} = $time;

        if ($this->usesTimestamps() && $this->getUpdatedAtColumn() !== null) {
            $this->{$this->getUpdatedAtColumn()} = $time;
            $columns[$this->getUpdatedAtColumn()] = $this->fromDateTime($time);
        }

        if (array_key_exists('deleted_by', $this->getAttributes())) {
            $columns['deleted_by'] = $this->getAttribute('deleted_by');
        }

        $query->update($columns);

        $this->syncOriginalAttributes(array_keys($columns));

        $this->fireModelEvent('trashed', false);
    }
}
