<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Models;

use DateTimeInterface;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

/**
 * 基底Modelクラスです。
 *
 * @author @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Models
 */
abstract class AppModel extends Model
{
    use HasFactory;
    use SoftDeletes;

    /**
     * @var bool キャッシュ設定
     */
    public bool $cached = false;

    /**
     * 複数代入不可能な属性設定
     *
     * @var string[]
     */
    protected $guarded = ['id'];

    /**
     * Carbonインスタンスへキャストする日付属性設定
     *
     * @var string[]
     */
    protected $casts = [
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
        'deleted_at' => 'datetime',
    ];

    /**
     * コンストラクタ
     *
     * @param array<string, mixed> $attributes カラムとデータの配列
     */
    public function __construct(array $attributes = [])
    {
        parent::__construct($attributes);
    }

    /**
     * {@inheritdoc}
     */
    protected function runSoftDelete()
    {
        $query = $this->setKeysForSaveQuery($this->newModelQuery());

        $time = $this->freshTimestamp();

        $columns = [$this->getDeletedAtColumn() => $this->fromDateTime($time)];

        $this->{$this->getDeletedAtColumn()} = $time;

        if ($this->usesTimestamps() && ! is_null($this->getUpdatedAtColumn())) {
            $this->{$this->getUpdatedAtColumn()} = $time;

            $columns[$this->getUpdatedAtColumn()] = $this->fromDateTime($time);
        }

        //削除者を更新します
        $columns['deleted_by'] = $this->deleted_id;

        $query->update($columns);

        $this->syncOriginalAttributes(array_keys($columns));

        $this->fireModelEvent('trashed', false);
    }

    /**
     * {@inheritdoc}
     */
    protected function serializeDate(DateTimeInterface $date)
    {
        return $date->format('Y-m-d H:i:s');
    }
}
