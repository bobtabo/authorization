<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Condition;

use App\Domain\Client\Enums\ClientStatus;
use App\Support\Repositories\Conditions\AbstractCondition;
use Carbon\Carbon;

/**
 * クライアントConditionクラスです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Domain\Client\Condition
 */
class ClientCondition extends AbstractCondition
{
    public ?string $name = null;
    public ?string $identifer = null;
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
