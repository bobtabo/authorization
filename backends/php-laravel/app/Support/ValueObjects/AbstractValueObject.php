<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\ValueObjects;

use App\Support\Traits\Assign;
use App\Support\Traits\Attribute;
use App\Support\Traits\Initialize;
use App\Support\ValueObject;

/**
 * 基底ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\ValueObject
 *
 * @method int|null getVersion()
 */
abstract class AbstractValueObject implements ValueObject
{
    use Assign;
    use Attribute;
    use Initialize;

    protected ?int $version = null;

    /**
     * コンストラクタ
     */
    public function __construct()
    {
        $this->initializer();
    }
}
