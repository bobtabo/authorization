<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Mappers;

use App\Domain\Staff\Entities\Staff;
use App\Domain\Staff\Enums\StaffRole;
use App\Domain\Staff\Enums\StaffStatus;

/**
 * スタッフ Entity を API レスポンス向け配列へ変換します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Mappers
 */
class StaffApiMapper
{
    /**
     * @return array<string, mixed>
     */
    public static function toListItem(Staff $staff): array
    {
        return [
            'id' => $staff->id,
            'name' => $staff->name,
            'email' => $staff->email,
            'role' => $staff->role instanceof StaffRole ? $staff->role->value : $staff->role,
            'status' => $staff->status instanceof StaffStatus ? $staff->status->value : ($staff->status ?? StaffStatus::Active->value),
        ];
    }
}
