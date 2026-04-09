<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Http\Requests;

use Illuminate\Contracts\Validation\Validator;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Support\Arr;
use Jenssegers\Agent\Agent;
use App\Support\Exceptions\ValidationException;

/**
 * 共通Requestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Http\Requests
 */
class AppRequest extends FormRequest
{
    /**
     * リクエスト利用許可を取得します。
     *
     * @return bool 利用可の場合 true を返します
     */
    public function authorize()
    {
        return true;
    }

    /**
     * 検証ルールを取得します。
     *
     * @return array<string, string> 検証ルール
     */
    public function rules()
    {
        return [];
    }

    /**
     * 検証エラーメッセージを取得します。
     *
     * @return array<string, string> 検証エラーメッセージ
     */
    public function messages()
    {
        return [];
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function all($keys = null)
    {
        $all = parent::all($keys);
        return array_merge($all, $this->getExtendValue());
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function input($key = null, $default = null)
    {
        if ($key !== null) {
            return parent::input($key, $default);
        }
        return array_merge(parent::input(), $this->getExtendValue());
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function failedValidation(Validator $validator)
    {
        /** @var \App\Support\Http\Validators\AppValidator $validator */
        $exception = (new ValidationException($validator))
            ->errorBag($this->errorBag)
            ->redirectTo($this->getRedirectUrl());

        throw $exception;
    }

    /**
     * 拡張データを取得します。
     *
     * @return array 拡張データ
     */
    protected function getExtendValue(): array
    {
        $agent = new Agent();
        $result = [
            'device' => $agent->device(),
            'platform' => $agent->platform(),
            'browser' => $agent->browser(),
            'user_agent' => $agent->getUserAgent(),
        ];

        if (!Arr::has($result, 'id') && !empty($this->route('id'))) {
            $result['id'] = $this->route('id');
        }

        $value = $this->header('X-Executor-Id');
        $result['executor_id'] = ($value !== null && $value !== '') ? (int)$value : null;

        return $result;
    }
}
