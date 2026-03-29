<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Auth;

use App\Domain\Auth\Entities\AuthUser;
use App\Domain\Auth\Repositories\AuthUserRepositoryInterface;
use App\UseCases\Common\AbstractService;
use Illuminate\Database\QueryException;

/**
 * 認証ユーザー参照のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Auth
 */
final class AuthApplicationService extends AbstractService
{
    /**
     * @param  AuthUserRepositoryInterface  $users  認証ユーザーRepository
     */
    public function __construct(
        private readonly AuthUserRepositoryInterface $users,
    ) {}

    /**
     * ID で認証ユーザーを1件取得します。
     *
     * @param  int  $id  ユーザーID
     * @return AuthUser|null 該当がなければ null
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function findUser(int $id): ?AuthUser
    {
        return $this->users->findById($id);
    }
}
