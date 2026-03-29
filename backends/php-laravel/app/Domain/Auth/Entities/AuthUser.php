<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Auth\Entities;

use app\Support\Entities\AbstractEntity;

/**
 * 認証ドメインのログイン主体の状態を表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Auth\Entities
 */
final readonly class AuthUser extends AbstractEntity
{
    /**
     * @param  int  $id  ユーザーID
     * @param  string  $name  表示名
     * @param  string  $email  メールアドレス
     */
    public function __construct(
        public int $id,
        public string $name,
        public string $email,
    ) {}
}
