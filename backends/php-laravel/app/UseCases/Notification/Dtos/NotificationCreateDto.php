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
 * 通知登録Dtoクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Notification\Dtos
 */
class NotificationCreateDto extends AbstractDto
{
    public ?int $messageType = null;
    public ?string $title = null;
    public ?string $message = null;
    public ?string $url = null;
}
