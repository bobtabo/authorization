/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.invitation.repositories;

/**
 * 招待認証Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public interface InvitationAuthRepository {

    /**
     * 招待トークンとロールを一時保存します。
     *
     * @param token 招待トークン
     * @param role 権限（1=管理者, 2=メンバー）
     * @param ttl 有効期限（秒）
     */
    void store(String token, int role, int ttl);

    /**
     * 招待トークンに紐づくロールを取得します。
     *
     * @param token 招待トークン
     * @return ロール、未保存の場合 null
     */
    Integer find(String token);

    /**
     * 招待トークンを削除します。
     *
     * @param token 招待トークン
     */
    void remove(String token);
}
