<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Gate\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * JWT 検証結果（Payload 相当）ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Gate\ValueObjects
 *
 * @method string|null getIss()
 * @method string|null getSub()
 * @method string|null getAud()
 * @method int getExp()
 * @method int getIat()
 * @method int getNbf()
 * @method string|null getJti()
 */
class GateVerifyVo extends AbstractValueObject
{
    use Getter;

    private ?string $iss = null;
    private ?string $sub = null;
    private ?string $aud = null;
    private int $exp = 0;
    private int $iat = 0;
    private int $nbf = 0;
    private ?string $jti = null;
}
