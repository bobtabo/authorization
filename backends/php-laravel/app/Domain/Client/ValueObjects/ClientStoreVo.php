<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\ValueObjects;

use App\Support\Mails\MailSend;
use Carbon\Carbon;

/**
 * クライアント登録・更新ValueObjectクラスです。
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
 * @method string|null getAccessToken()
 * @method int|null getStatus()
 * @method Carbon|null getStartAt()
 * @method Carbon|null getStopAt()
 * @method Carbon|null getCreatedAt()
 * @method Carbon|null getUpdatedAt()
 */
class ClientStoreVo extends MailSend
{
    protected ?int $id = null;
    protected ?string $name = null;
    protected ?string $identifier = null;
    protected ?string $postCode = null;
    protected ?string $pref = null;
    protected ?string $city = null;
    protected ?string $address = null;
    protected ?string $building = null;
    protected ?string $tel = null;
    protected ?string $email = null;
    protected ?string $accessToken = null;
    protected ?int $status = null;
    protected ?Carbon $startAt = null;
    protected ?Carbon $stopAt = null;
    protected ?Carbon $createdAt = null;
    protected ?Carbon $updatedAt = null;
}
