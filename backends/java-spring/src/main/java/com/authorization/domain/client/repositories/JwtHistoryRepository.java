/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.repositories;

import com.authorization.domain.client.condition.JwtHistoryCondition;
import com.authorization.domain.client.entities.JwtHistory;
import java.util.List;

/**
 * JWT履歴Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public interface JwtHistoryRepository {

    /**
     * クライアントIDに紐づくJWT履歴の総件数を返します。
     *
     * @param condition 検索条件
     * @return 総件数
     */
    int countByClientId(JwtHistoryCondition condition);

    /**
     * クライアントIDに紐づくJWT履歴を返します。
     *
     * @param condition 検索条件
     * @return エンティティ一覧
     */
    List<JwtHistory> findByClientId(JwtHistoryCondition condition);

    /**
     * JWT履歴を保存します。
     *
     * @param entity エンティティ
     * @return 保存後のエンティティ
     */
    JwtHistory persist(JwtHistory entity);
}
