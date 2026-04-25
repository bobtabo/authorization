/*
 * スタッフリポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff
import org.jetbrains.exposed.sql.Database

/**
 * Exposed を使用したスタッフリポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ExposedStaffRepository(private val db: Database) : Repository {

    /**
     * 検索条件に一致するスタッフ一覧を取得します。
     *
     * @param cond 検索条件
     * @return スタッフ一覧
     */
    override suspend fun findByCondition(cond: Condition): List<Staff> = TODO()

    /**
     * 指定した ID のスタッフを取得します。
     *
     * @param id スタッフ ID
     * @return スタッフ、または null
     */
    override suspend fun findById(id: Long): Staff? = TODO()

    /**
     * プロバイダー情報に一致するスタッフを取得します。
     *
     * @param provider プロバイダー種別
     * @param providerId プロバイダー ID
     * @return スタッフ、または null
     */
    override suspend fun findByProvider(provider: Int, providerId: String): Staff? = TODO()

    /**
     * アクティブなスタッフ全員を取得します。
     *
     * @return スタッフ一覧
     */
    override suspend fun findAllActive(): List<Staff> = TODO()

    /**
     * スタッフを保存します（新規登録・更新）。
     *
     * @param s スタッフエンティティ
     * @return 保存後のスタッフエンティティ
     */
    override suspend fun save(s: Staff): Staff = TODO()

    /**
     * スタッフのロールを更新します。
     *
     * @param id スタッフ ID
     * @param role 新しいロール
     * @param updatedBy 更新者スタッフ ID
     * @return 更新成功なら true
     */
    override suspend fun updateRole(id: Long, role: Int, updatedBy: Long): Boolean = TODO()

    /**
     * スタッフを論理削除します。
     *
     * @param id スタッフ ID
     * @param deletedBy 削除者スタッフ ID
     * @return 削除成功なら true
     */
    override suspend fun softDelete(id: Long, deletedBy: Long): Boolean = TODO()

    /**
     * 論理削除されたスタッフを復元します。
     *
     * @param id スタッフ ID
     * @return 復元成功なら true
     */
    override suspend fun restore(id: Long): Boolean = TODO()
}
