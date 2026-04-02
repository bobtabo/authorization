<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Dtos;

use App\Support\Dto;
use App\Support\Traits\Assign;
use App\Support\Traits\Attribute;
use App\Support\Traits\Initialize;

/**
 * 基底DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Dtos
 */
abstract class AbstractDto implements Dto
{
    use Assign;
    use Attribute;
    use Initialize;

    /**
     * @var int|null 処理実行者ID
     */
    public ?int $executorId = null;

    /**
     * @var int|null バージョン
     */
    public ?int $version = null;

    /**
     * コンストラクタ
     */
    public function __construct()
    {
        $this->initializer();
    }
}
