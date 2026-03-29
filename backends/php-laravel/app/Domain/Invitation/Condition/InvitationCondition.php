<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Invitation\Condition;

use App\Support\Repositories\Conditions\AbstractCondition;

/**
 * 招待Conditionクラスです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Domain\Invitation\Condition
 */
class InvitationCondition extends AbstractCondition
{
    public ?string $token = null;
}
