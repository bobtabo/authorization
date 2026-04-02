<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Requests\Client;

use App\Support\Http\Requests\AppRequest;

/**
 * クライアント更新リクエストです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Requests\Client
 */
class UpdateClientRequest extends AppRequest
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
            'id' => ['required', 'integer'],
            'name' => ['sometimes', 'string', 'max:255'],
            'identifier' => ['sometimes', 'string', 'max:255'],
            'post_code' => ['sometimes', 'string', 'max:8'],
            'pref' => ['sometimes', 'string', 'max:50'],
            'city' => ['sometimes', 'string', 'max:100'],
            'address' => ['sometimes', 'string', 'max:255'],
            'building' => ['nullable', 'string', 'max:255'],
            'tel' => ['sometimes', 'string', 'max:255'],
            'email' => ['sometimes', 'string', 'email', 'max:255'],
            'status' => ['sometimes', 'integer'],
        ];
    }
}
