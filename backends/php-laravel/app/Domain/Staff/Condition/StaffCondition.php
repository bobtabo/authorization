<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Condition;

use App\Domain\Staff\Enums\Provider;
use App\Domain\Staff\Enums\StaffRole;
use App\Domain\Staff\Enums\StaffStatus;
use App\Support\Repositories\Conditions\AbstractCondition;
use Carbon\Carbon;

/**
 * スタッフConditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Condition
 */
class StaffCondition extends AbstractCondition
{
    public ?int $id = null;
    public ?string $name = null;
    public ?string $email = null;
    public ?Provider $provider = null;
    public ?string $providerId = null;
    public ?string $avater = null;
    public ?StaffRole $role = null;
    public ?StaffStatus $status = null;
    public ?Carbon $lastLoginAt = null;

    /**
     * 一覧検索用キーワード（名前・識別子の部分一致）。API の keyword からマッピングします。
     */
    public ?string $keyword = null;

    /**
     * 権限コードの一覧（空は無条件）
     *
     * @var array<int, int>
     */
    public array $roles = [];

    /**
     * 状態コードの一覧（空は無条件）
     *
     * @var array<int, int>
     */
    public array $statuses = [];
}
