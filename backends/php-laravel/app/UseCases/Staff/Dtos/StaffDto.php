<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Staff\Dtos;

use App\Domain\Staff\Enums\StaffRole;
use App\Support\Dtos\PagerDto;

/**
 * スタッフ API 用の入力 DTO です（一覧・権限更新・削除・単体取得で共用します）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Staff\Dtos
 */
class StaffDto extends PagerDto
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
     * 権限更新時の権限コード
     */
    public ?StaffRole $role = null;

    /**
     * 操作を実行したスタッフID。
     */
    public ?int $executorId = null;

    /** {@inheritdoc} */
    public function assign(array $values, array $convert = [], array $excludes = []): mixed
    {
        if (isset($values['sort_type'])) {
            $values['sort_type'] = strtoupper($values['sort_type']);
        }
        return parent::assign($values, $convert, $excludes);
    }
}
