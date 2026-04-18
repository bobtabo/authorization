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
 * 通知DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Notification\Dtos
 */
class NotificationDto extends AbstractDto
{
    public ?int $staffId = null;
    public ?string $cursor = null;
    public int $limit = 20;

    /**
     * 一括更新対象の通知 ID（空配列は ids 未指定）。
     *
     * @var list<int>
     */
    public array $ids = [];
    public bool $all = false;
    public ?int $notificationId = null;
}
