<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Http\Validators;

use Illuminate\Validation\Validator as BaseValidator;
use Str;

/**
 * 基底Validatorクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Http\Validators
 */
class AppValidator extends BaseValidator
{
    /**
     * 検証エラープロパティを取得します。
     *
     * @return string 検証エラーフィールド
     */
    final public function getFailedProperty(): string
    {
        return key($this->failedRules);
    }

    /**
     * 検証エラールールを取得します。
     *
     * @return string 検証エラールール
     */
    final public function getFailedRule(): string
    {
        return Str::snake(key(current($this->failedRules)));
    }

    /**
     * 検証エラールールを取得します。
     *
     * @return array 検証エラールール
     */
    final public function getFailedRules(): array
    {
        return $this->failedRules;
    }

    /**
     * 検証エラーメッセージを取得します。
     *
     * @return string 検証エラーメッセージ
     */
    final public function getFailedMessage(): string
    {
        return current($this->getMessageBag()->getMessages()[$this->getFailedField()]);
    }

    /**
     * 検証エラーメッセージを取得します。
     *
     * @return array 検証エラーメッセージ
     */
    final public function getFailedMessages(): array
    {
        return $this->getMessageBag()->getMessages();
    }
}
