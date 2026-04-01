<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\ValueObjects;

use App\Support\ValueObjects\AbstractValueObject;
use Carbon\Carbon;

/**
 * クライアント詳細の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 *
 * @method int|null getId()
 * @method string|null getName()
 * @method string|null getIdentifier()
 * @method string|null getPostCode()
 * @method string|null getPref()
 * @method string|null getCity()
 * @method string|null getAddress()
 * @method string|null getBuilding()
 * @method string|null getTel()
 * @method string|null getEmail()
 * @method int|null getStatus()
 * @method Carbon|null getStartAt()
 * @method Carbon|null getStopAt()
 * @method Carbon|null getCreatedAt()
 * @method Carbon|null getUpdatedAt()
 */
class ClientDetailVo extends AbstractValueObject
{
    private ?int $id = null;
    private ?string $name = null;
    private ?string $identifier = null;
    private ?string $postCode = null;
    private ?string $pref = null;
    private ?string $city = null;
    private ?string $address = null;
    private ?string $building = null;
    private ?string $tel = null;
    private ?string $email = null;
    private ?int $status = null;
    private ?Carbon $startAt = null;
    private ?Carbon $stopAt = null;
    private ?Carbon $createdAt = null;
    private ?Carbon $updatedAt = null;
}
