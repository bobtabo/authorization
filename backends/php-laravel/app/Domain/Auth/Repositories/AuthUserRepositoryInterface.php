<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Auth\Repositories;

use App\Domain\Auth\Entities\Auth;

/**
 * 認証ユーザーを永続化層から取得するRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Auth\Repositories
 */
interface AuthUserRepositoryInterface
{
    /**
     * ID で認証ユーザーを1件取得します。
     *
     * @param  int  $id  ユーザーID
     * @return Auth|null 該当がなければ null
     */
    public function findById(int $id): ?Auth;

    /**
     * メールアドレスで認証ユーザーを1件取得します。
     *
     * @param  string  $email  メールアドレス
     * @return Auth|null 該当がなければ null
     */
    public function findByEmail(string $email): ?Auth;
}
