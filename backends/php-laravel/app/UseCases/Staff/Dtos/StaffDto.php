<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Staff\Dtos;

use App\Support\Dtos\AbstractDto;

/**
 * スタッフ API 用の入力 DTO です（一覧・権限更新・削除・単体取得で共用します）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Staff\Dtos
 */
class StaffDto extends AbstractDto
{
    public ?int $id = null;

    public ?string $keyword = null;

    /**
     * 一覧検索用の権限コード（複数）。空配列は無条件。
     *
     * @var list<int>
     */
    public array $roles = [];

    /**
     * 一覧検索用の状態コード（複数）。空配列は無条件。
     *
     * @var list<int>
     */
    public array $statuses = [];

    /**
     * 権限更新時の権限コード（{@see \App\Domain\Staff\Enums\StaffRole} の値）。
     */
    public ?int $role = null;
}
