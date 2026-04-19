<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Notification\Repositories;

use App\Domain\Notification\Condition\NotificationCondition;
use App\Domain\Notification\Entities\Notification;
use Illuminate\Support\Collection;

/**
 * 通知一覧のページング取得と件数集計・更新を担うRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\Repositories
 */
interface NotificationRepository
{
    /**
     * カーソル付きで通知一覧ページを取得します。
     *
     * @param NotificationCondition $condition 検索条件
     * @return Collection コレクション
     */
    public function listPage(NotificationCondition $condition): Collection;

    /**
     * 通知件数の集計を取得します。
     *
     * @param NotificationCondition $condition 検索条件
     * @return int 件数
     */
    public function counts(NotificationCondition $condition): int;

    /**
     * 通知を既読更新します。
     *
     * @param NotificationCondition $condition 検索条件
     * @return int 更新件数
     */
    public function updateRead(NotificationCondition $condition): int;

    /**
     * 通知を登録します。
     *
     * @param Notification $entity エンティティ
     * @return void
     */
    public function persist(Notification $entity): void;
}
