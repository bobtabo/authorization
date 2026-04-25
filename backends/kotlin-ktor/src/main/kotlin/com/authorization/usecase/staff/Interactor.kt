/*
 * スタッフユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.staff

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.ListItem
import com.authorization.domain.staff.Repository

/**
 * スタッフユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(private val repo: Repository) {

    /**
     * 検索条件に一致するスタッフ一覧を取得します。
     *
     * @param cond 検索条件
     * @return スタッフ一覧
     */
    suspend fun findByCondition(cond: Condition): List<ListItem> = TODO()

    /**
     * スタッフのロールを更新します。
     *
     * @param dto ロール更新 DTO
     */
    suspend fun updateRole(dto: UpdateRoleDto): Unit = TODO()

    /**
     * 論理削除されたスタッフを復元します。
     *
     * @param id スタッフ ID
     */
    suspend fun restore(id: Long): Unit = TODO()

    /**
     * スタッフを論理削除します。
     *
     * @param dto 削除 DTO
     */
    suspend fun destroy(dto: DestroyDto): Unit = TODO()
}
