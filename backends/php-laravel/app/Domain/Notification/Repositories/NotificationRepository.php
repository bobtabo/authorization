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
     * @param int $staffId 対象スタッフID
     * @param string|null $cursor 次ページカーソル（先頭は null）
     * @param int $limit 1ページあたりの最大件数
     * @return array{items: list<Notification>, next_cursor: ?string} 一覧と次カーソル
     */
    public function listPage(int $staffId, ?string $cursor, int $limit): array;

    /**
     * 通知件数の集計を取得します。
     *
     * @param int $staffId 対象スタッフID
     * @return array<string, int> 種別ごとの件数
     */
    public function counts(int $staffId): array;

    /**
     * 指定 ID の通知を一括で既読などの状態に更新します。
     *
     * @param int $staffId 対象スタッフID
     * @param list<int> $ids 対象 ID（空配列は未使用）
     * @param bool $all 全件対象
     * @return int 更新件数
     */
    public function bulkMarkRead(int $staffId, array $ids, bool $all): int;

    /**
     * 単一通知を部分更新します。
     *
     * @param int $id 通知 ID
     * @param array<string, mixed> $attributes 更新属性（read 等）
     * @return bool 成功時 true
     */
    public function patch(int $id, array $attributes): bool;

    /**
     * 通知を1件登録します。
     *
     * @param int $staffId 対象スタッフID
     * @param int $messageType メッセージ種類
     * @param string $title タイトル
     * @param string $message メッセージ
     * @param int $executorId 登録者ID
     * @param string|null $url 遷移先URL（省略可）
     * @return void
     */
    public function store(int $staffId, int $messageType, string $title, string $message, int $executorId, ?string $url = null): void;
}
