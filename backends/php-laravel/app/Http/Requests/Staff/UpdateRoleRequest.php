<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Requests\Staff;

use App\Support\Http\Requests\AppRequest;

/**
 * スタッフ権限更新Requestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Requests\Staff
 */
class UpdateRoleRequest extends AppRequest
{
    /**
     * {@inheritdoc}
     */
    public function authorize(): bool
    {
        return true;
    }

    /**
     * {@inheritdoc}
     *
     * @return array<string, array<int, string>|string>
     */
    public function rules(): array
    {
        return [
            'role' => ['required', 'integer'],
            'executor_id' => ['required', 'integer'],
        ];
    }
}
