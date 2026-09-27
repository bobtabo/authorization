/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.gate.repositories;

/**
 * 認可Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public interface GateRepository {

    /**
     * JWT をキャッシュします。
     *
     * @param identifier クライアント識別名
     * @param memberId クライアント会員ID
     * @param token JWT 文字列
     * @param ttl 有効期限（秒）
     */
    void putJwt(String identifier, String memberId, String token, int ttl);

    /**
     * キャッシュ済み JWT を取得します。
     *
     * @param identifier クライアント識別名
     * @param memberId クライアント会員ID
     * @return キャッシュされた JWT 文字列、未キャッシュの場合 null
     */
    String getJwt(String identifier, String memberId);
}
