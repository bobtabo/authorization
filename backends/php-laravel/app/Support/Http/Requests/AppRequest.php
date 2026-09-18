<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Http\Requests;

use App\Support\Http\Validators\AppValidator;
use Illuminate\Contracts\Validation\Validator;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Support\Arr;
use Illuminate\Validation\ValidationException;
use Jenssegers\Agent\Agent;

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
        $extended = array_merge(parent::input(), $this->getExtendValue());
        if ($key !== null) {
            return $extended[$key] ?? $default;
        }

        return $extended;
    }

    /**
     * クエリ文字列から配列パラメータを取得します。
     *
     * ブラケット付き（`key[]=1&key[]=2`）・ブラケット無しの繰り返しキー
     * （`key=1&key=2`、OpenAPI仕様の `style: form, explode: true`）の
     * どちらの形式でも配列として取得できます。PHPの標準的なクエリパース
     * （parse_str/$_GET）はブラケット無しの繰り返しキーを配列として扱わず、
     * 最後の値で上書きしてしまうため、生のクエリ文字列を自前で解析します。
     *
     * @param  string  $key  パラメータ名
     * @return array<int, string> 値の一覧（該当パラメータが無ければ空配列）
     */
    public function arrayQuery(string $key): array
    {
        $queryString = (string) $this->server->get('QUERY_STRING', '');
        if ($queryString === '') {
            return [];
        }

        $values = [];
        foreach (explode('&', $queryString) as $pair) {
            if ($pair === '') {
                continue;
            }
            [$rawKey, $rawValue] = array_pad(explode('=', $pair, 2), 2, '');
            $decodedKey = urldecode($rawKey);
            if ($decodedKey === $key || $decodedKey === $key . '[]') {
                $values[] = urldecode($rawValue);
            }
        }

        return $values;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function failedValidation(Validator $validator)
    {
        /** @var AppValidator $validator */
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

        if (!Arr::has($result, 'identifier') && !empty($this->route('identifier'))) {
            $result['identifier'] = $this->route('identifier');
        }

        $value = $this->header('X-Executor-Id');
        $result['executor_id'] = ($value !== null && $value !== '') ? (int) $value : null;

        return $result;
    }
}
