<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Entities;

/**
 * スタッフ集約のルートを表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Entities
 */
final readonly class Staff
{
    /**
     * @param  int  $id  スタッフID
     * @param  string  $name  表示名
     * @param  string  $email  メールアドレス
     * @param  int  $activeValue  状態コード
     * @param  int  $roleValue  権限コード
     */
    public function __construct(
        public int $id,
        public string $name,
        public string $email,
        public int $activeValue,
        public int $roleValue,
    ) {}
}
