<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Notification\Dtos;

use App\Support\Dtos\AbstractDto;

/**
 * 通知 API 用の入力 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Notification\Dtos
 */
class NotificationDto extends AbstractDto
{
    public ?string $cursor = null;

    public int $limit = 20;

    /**
     * 一括更新対象の通知 ID（null は ids 未指定）。
     *
     * @var list<string>|null
     */
    public ?array $ids = null;

    public bool $all = false;

    public ?string $notificationId = null;

    /**
     * 単一通知の部分更新内容。
     *
     * @var array<string, mixed>
     */
    public array $attributes = [];
}
