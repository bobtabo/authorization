/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.invitation.repositories;

import com.authorization.domain.invitation.condition.InvitationCondition;
import com.authorization.domain.invitation.entities.Invitation;

/**
 * 招待Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public interface InvitationRepository {

    /**
     * 指定ロールの招待情報を取得します。
     *
     * @param role 権限（1=管理者, 2=メンバー）
     * @return エンティティ、未設定時は null
     */
    Invitation getCurrentByRole(int role);

    /**
     * 招待を更新します。
     *
     * @param entity 招待エンティティ
     * @return 更新された招待
     */
    Invitation persist(Invitation entity);

    /**
     * トークンから招待情報を解決します（未登録・不正なら null）。
     *
     * @param condition 検索条件
     * @return エンティティ、該当がなければ null
     */
    Invitation findByToken(InvitationCondition condition);
}
