<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Notification\Repositories;

use App\Domain\Notification\Entities\Notification;

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

    /**
     * 指定 ID の通知を一括で既読などの状態に更新します（永続化未実装時は no-op）。
     *
     * @param  list<string>|null  $ids  対象 ID（null は未使用）
     * @param  bool  $all  全件対象
     * @return int 更新件数（スタブでは 0）
     */
    public function bulkMarkRead(?array $ids, bool $all): int;

    /**
     * 単一通知を部分更新します。
     *
     * @param  string  $id  通知 ID
     * @param  array<string, mixed>  $attributes  更新属性（read 等）
     * @return bool 成功時 true（スタブでは常に true）
     */
    public function patch(string $id, array $attributes): bool;
}
