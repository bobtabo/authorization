/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.repositories;

import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.domain.staff.entities.Staff;
import java.util.List;

/**
 * スタッフRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public interface StaffRepository {

    /**
     * スタッフリストを検索します。
     *
     * @param condition 検索条件
     * @return エンティティ一覧
     */
    List<Staff> findByCondition(StaffCondition condition);

    /**
     * 条件でスタッフ件数を取得します。
     *
     * @param condition 検索条件
     * @return 件数
     */
    int countByCondition(StaffCondition condition);

    /**
     * ID でスタッフを取得します。
     *
     * @param condition 検索条件（id を設定すること）
     * @return エンティティ、存在しない場合は null
     */
    Staff findById(StaffCondition condition);

    /**
     * プロバイダー情報でスタッフを取得します。
     *
     * @param condition 検索条件
     * @return エンティティ、存在しない場合は null
     */
    Staff findByProvider(StaffCondition condition);

    /**
     * スタッフを新規登録または更新して永続化します。
     *
     * @param entity 永続化するエンティティ（id 未設定で新規）
     * @return 保存後のエンティティ
     */
    Staff persist(Staff entity);

    /**
     * スタッフを論理削除します。
     *
     * @param entity エンティティ
     * @return 対象が存在して削除できた場合 true
     */
    boolean deleteById(Staff entity);

    /**
     * スタッフの論理削除を復元します。
     *
     * @param entity エンティティ
     * @return 対象が存在して復元できた場合 true
     */
    boolean restoreById(Staff entity);

    /**
     * 有効なスタッフ全件を取得します（論理削除済み除外）。
     *
     * @return エンティティ一覧
     */
    List<Staff> findAllActive();
}
