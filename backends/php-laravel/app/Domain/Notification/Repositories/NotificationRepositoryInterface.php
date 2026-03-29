<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Notification\Repositories;

use App\Domain\Notification\Entities\Notification;

/**
 * 通知一覧のページング取得と件数集計を担うRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\Repositories
 */
interface NotificationRepositoryInterface
{
    /**
     * カーソル付きで通知一覧ページを取得します。
     *
     * @param  string|null  $cursor  次ページカーソル（先頭は null）
     * @param  int  $limit  1ページあたりの最大件数
     * @return array{items: list<Notification>, next_cursor: ?string} 一覧と次カーソル
     */
    public function listPage(?string $cursor, int $limit): array;

    /**
     * 通知件数の集計を取得します。
     *
     * @return array<string, int> 種別ごとの件数
     */
    public function counts(): array;
}
