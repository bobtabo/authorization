<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\Entities;

use App\Domain\Client\Enums\ClientStatus;
use App\Support\Entities\AbstractEntity;
use Carbon\Carbon;

/**
 * クライアントEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Entities
 */
class Client extends AbstractEntity
{
    public ?int $id = null;
    public ?string $name = null;
    public ?string $identifier = null;
    public ?string $postCode = null;
    public ?string $pref = null;
    public ?string $city = null;
    public ?string $address = null;
    public ?string $building = null;
    public ?string $tel = null;
    public ?string $email = null;
    public ?string $accessToken = null;
    public ?string $privateKey = null;
    public ?string $publicKey = null;
    public ?string $fingerprint = null;
    public ?ClientStatus $status = null;
    public ?Carbon $startAt = null;
    public ?Carbon $stopAt = null;
}
