/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.repositories;

import com.authorization.domain.client.condition.ClientCondition;
import com.authorization.domain.client.entities.Client;
import java.util.List;

/**
 * クライアントRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public interface ClientRepository {

    /**
     * クライアントリストを検索します。
     *
     * @param condition 検索条件
     * @return エンティティ一覧
     */
    List<Client> findByCondition(ClientCondition condition);

    /**
     * 検索条件に合致するクライアントの総件数を返します。
     *
     * @param condition 検索条件
     * @return 総件数
     */
    int countByCondition(ClientCondition condition);

    /**
     * クライアントを取得します。
     *
     * @param condition 検索条件
     * @return エンティティ、存在しない場合は null
     */
    Client findById(ClientCondition condition);

    /**
     * クライアントを新規登録または更新して永続化します。
     *
     * @param entity エンティティ（id 未設定で新規）
     * @return 保存後のエンティティ
     */
    Client persist(Client entity);

    /**
     * アクセストークンでクライアントを取得します。
     *
     * @param condition 検索条件
     * @return エンティティ、存在しない場合は null
     */
    Client findByAccessToken(ClientCondition condition);

    /**
     * クライアント識別名でクライアントを取得します。
     *
     * @param condition 検索条件
     * @return エンティティ、存在しない場合は null
     */
    Client findByIdentifier(ClientCondition condition);

    /**
     * クライアントを論理削除します。
     *
     * @param entity エンティティ
     * @return 対象が存在して削除できた場合 true
     */
    boolean deleteById(Client entity);
}
