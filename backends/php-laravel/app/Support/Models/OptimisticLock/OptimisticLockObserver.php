<?php

/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */

declare(strict_types=1);

namespace Sii\Selloop\Core\Observers;

use App\Support\Exceptions\AppException;
use App\Support\Models\AppModel;

/**
 * 楽観的ロックObserverクラスです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package Sii\Selloop\Core\Observers
 */
class OptimisticLockObserver
{
    /**
     * 楽観的ロックチェック用の一時プロパティ
     */
    public const string OPTIMISTIC_LOCK_COLUMN = 'version_optimistic_lock_column';

    /**
     * 登録前の排他制御を行います。
     *
     * @param AppModel $model モデル: void
     * @return void
     */
    public function created(AppModel $model): void
    {
        //
    }

    /**
     * 更新前の排他制御を行います。
     *
     * @param AppModel $model モデル
     * @return void
     */
    public function updating(AppModel $model): void
    {
        if (!$this->isProperty($model)) {
            return;
        }

        $this->exclusion($model);
        $model->version++;
    }

    /**
     * 論理削除前の排他制御を行います。
     *
     * @param AppModel $model モデル
     * @return void
     */
    public function deleting(AppModel $model): void
    {
        if (!$this->isProperty($model)) {
            return;
        }

        $this->exclusion($model);
        $model->version++;
    }

    /**
     * 排他を確認します。
     *
     * @param AppModel $model モデル
     * @param $msgType
     * @return void
     */
    private function exclusion(AppModel $model): void
    {
        if (!$this->isProperty($model)) {
            return;
        }

        //現在バージョンを取得します
        $key = $model->getKeyName();
        $current = $model->withTrashed()->find($model->$key);

        //更新されているかチェック
        if ($model->{self::OPTIMISTIC_LOCK_COLUMN} != $current->version) {
            //楽観ロック
            throw AppException::internal('optimistic lock');
        }

        unset($model->{self::OPTIMISTIC_LOCK_COLUMN});
    }

    /**
     * 楽観的ロック用の一時プロパティがあるかチェックする
     *
     * @param AppModel $model
     * @return bool true:ある
     */
    private function isProperty(AppModel $model): bool
    {
        return array_key_exists('version', $model->getAttributes());
    }
}
